package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDutyPeriodEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708097042)
        val items = mutableListOf<ScheduledDutyPeriodEstimate>()
        for (index in 0 until 5) {
            items.add(newScheduledDutyPeriodEstimate("scheduled-duty-period-estimate-$index", "sample"))
        }
        val report = buildScheduledDutyPeriodEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
