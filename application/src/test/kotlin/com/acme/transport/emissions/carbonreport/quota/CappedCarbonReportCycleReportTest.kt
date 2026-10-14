package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCarbonReportCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709475165)
        val items = mutableListOf<CappedCarbonReportCycle>()
        for (index in 0 until 4) {
            items.add(newCappedCarbonReportCycle("capped-carbon-report-cycle-$index", "sample"))
        }
        val report = buildCappedCarbonReportCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
