package dev.elian.guard.rules;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

/**
 * 실행된 쿼리 수를 센다.
 *
 * <p>R8(반복문 안 단건 조회 금지)은 <b>정적 분석으로 못 잡는다.</b>
 * 반복문 안의 호출을 바이트코드에서 판정하기 어렵고, 무엇보다 <b>반복 횟수를 모른다.</b>
 * 그래서 런타임에 실제 쿼리 수를 세는 쪽이 정확하다.
 *
 * <p>기준은 절대값이 아니라 <b>건수에 비례하는가</b>로 잡는다.
 * 100건에 3회면 정상이고, 100건에 101회면 N+1 이다.
 */
public final class QueryCounter {

    private final Statistics statistics;

    public QueryCounter(SessionFactory sessionFactory) {
        this.statistics = sessionFactory.getStatistics();
        if (!statistics.isStatisticsEnabled()) {
            throw new IllegalStateException(
                    "hibernate.generate_statistics 가 꺼져 있다. 쿼리 수를 셀 수 없다");
        }
    }

    public void reset() {
        statistics.clear();
    }

    public long count() {
        return statistics.getPrepareStatementCount();
    }

    /**
     * 처리 건수에 비례해 쿼리가 늘어나는지 본다.
     *
     * @param itemCount 처리한 건수
     * @param maxPerItem 건당 허용 쿼리 수. 벌크로 처리했다면 0 에 가까워야 한다
     * @param fixedOverhead 건수와 무관한 고정 쿼리 수 (조회 1회 등)
     */
    public void assertNotPerItem(int itemCount, double maxPerItem, int fixedOverhead) {
        long actual = count();
        double allowed = fixedOverhead + itemCount * maxPerItem;
        if (actual > allowed) {
            throw new AssertionError(
                    "N+1 의심 — %d건 처리에 쿼리 %d회. 허용 %.0f회(고정 %d + 건당 %.2f). 건당 %.2f회 나왔다"
                            .formatted(itemCount, actual, allowed, fixedOverhead, maxPerItem,
                                    itemCount == 0 ? 0 : (double) actual / itemCount));
        }
    }
}
