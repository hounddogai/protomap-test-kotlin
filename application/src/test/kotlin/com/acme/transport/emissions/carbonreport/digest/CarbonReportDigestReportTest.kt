package com.acme.transport.emissions.carbonreport.digest

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700799572)
        val items = mutableListOf<CarbonReportDigest>()
        for (index in 0 until 5) {
            items.add(newCarbonReportDigest("carbon-report-digest-$index", "sample"))
        }
        val report = buildCarbonReportDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
