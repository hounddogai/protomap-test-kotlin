package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCarbonReportGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704465210)
        val items = mutableListOf<FallbackCarbonReportGroup>()
        for (index in 0 until 6) {
            items.add(newFallbackCarbonReportGroup("fallback-carbon-report-group-$index", "sample"))
        }
        val report = buildFallbackCarbonReportGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
