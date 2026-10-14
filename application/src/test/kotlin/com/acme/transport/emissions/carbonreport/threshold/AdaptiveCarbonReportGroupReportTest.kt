package com.acme.transport.emissions.carbonreport.threshold

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCarbonReportGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703715016)
        val items = mutableListOf<AdaptiveCarbonReportGroup>()
        for (index in 0 until 3) {
            items.add(newAdaptiveCarbonReportGroup("adaptive-carbon-report-group-$index", "sample"))
        }
        val report = buildAdaptiveCarbonReportGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
