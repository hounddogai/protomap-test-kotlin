package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDutyPeriodEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700708478)
        val items = mutableListOf<StrictDutyPeriodEstimate>()
        for (index in 0 until 3) {
            items.add(newStrictDutyPeriodEstimate("strict-duty-period-estimate-$index", "sample"))
        }
        val report = buildStrictDutyPeriodEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
