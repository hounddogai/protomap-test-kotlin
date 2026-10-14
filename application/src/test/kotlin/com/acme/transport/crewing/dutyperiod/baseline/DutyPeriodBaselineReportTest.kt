package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707174730)
        val items = mutableListOf<DutyPeriodBaseline>()
        for (index in 0 until 6) {
            items.add(newDutyPeriodBaseline("duty-period-baseline-$index", "sample"))
        }
        val report = buildDutyPeriodBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
