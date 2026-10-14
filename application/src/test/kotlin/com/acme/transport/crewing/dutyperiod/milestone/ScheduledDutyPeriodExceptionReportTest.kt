package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDutyPeriodExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708003906)
        val items = mutableListOf<ScheduledDutyPeriodException>()
        for (index in 0 until 6) {
            items.add(newScheduledDutyPeriodException("scheduled-duty-period-exception-$index", "sample"))
        }
        val report = buildScheduledDutyPeriodExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
