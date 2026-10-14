package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCarbonReportGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701055158)
        val items = mutableListOf<CappedCarbonReportGroup>()
        for (index in 0 until 4) {
            items.add(newCappedCarbonReportGroup("capped-carbon-report-group-$index", "sample"))
        }
        val report = buildCappedCarbonReportGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
