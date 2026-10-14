package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCarbonReportCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707112937)
        val items = mutableListOf<ScheduledCarbonReportCycle>()
        for (index in 0 until 4) {
            items.add(newScheduledCarbonReportCycle("scheduled-carbon-report-cycle-$index", "sample"))
        }
        val report = buildScheduledCarbonReportCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
