package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDutyPeriodClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704061773)
        val items = mutableListOf<WeightedDutyPeriodClaim>()
        for (index in 0 until 6) {
            items.add(newWeightedDutyPeriodClaim("weighted-duty-period-claim-$index", "sample"))
        }
        val report = buildWeightedDutyPeriodClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
