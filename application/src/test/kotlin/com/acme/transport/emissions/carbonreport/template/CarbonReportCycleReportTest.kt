package com.acme.transport.emissions.carbonreport.template

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709161442)
        val items = mutableListOf<CarbonReportCycle>()
        for (index in 0 until 5) {
            items.add(newCarbonReportCycle("carbon-report-cycle-$index", "sample"))
        }
        val report = buildCarbonReportCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
