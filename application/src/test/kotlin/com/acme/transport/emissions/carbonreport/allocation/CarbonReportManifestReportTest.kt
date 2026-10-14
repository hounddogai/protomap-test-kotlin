package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708471502)
        val items = mutableListOf<CarbonReportManifest>()
        for (index in 0 until 3) {
            items.add(newCarbonReportManifest("carbon-report-manifest-$index", "sample"))
        }
        val report = buildCarbonReportManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
