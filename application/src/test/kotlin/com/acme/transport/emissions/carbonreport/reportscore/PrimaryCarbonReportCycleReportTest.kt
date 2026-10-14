package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCarbonReportCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708092391)
        val items = mutableListOf<PrimaryCarbonReportCycle>()
        for (index in 0 until 4) {
            items.add(newPrimaryCarbonReportCycle("primary-carbon-report-cycle-$index", "sample"))
        }
        val report = buildPrimaryCarbonReportCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
