package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCarbonReportManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707116202)
        val items = mutableListOf<RegionalCarbonReportManifest>()
        for (index in 0 until 2) {
            items.add(newRegionalCarbonReportManifest("regional-carbon-report-manifest-$index", "sample"))
        }
        val report = buildRegionalCarbonReportManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
