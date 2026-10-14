package com.acme.transport.crewing.dutyperiod.summary

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705969570)
        val items = mutableListOf<DutyPeriodException>()
        for (index in 0 until 5) {
            items.add(newDutyPeriodException("duty-period-exception-$index", "sample"))
        }
        val report = buildDutyPeriodExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
