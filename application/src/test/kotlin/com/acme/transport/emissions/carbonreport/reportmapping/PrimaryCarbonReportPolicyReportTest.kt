package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCarbonReportPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707746076)
        val items = mutableListOf<PrimaryCarbonReportPolicy>()
        for (index in 0 until 6) {
            items.add(newPrimaryCarbonReportPolicy("primary-carbon-report-policy-$index", "sample"))
        }
        val report = buildPrimaryCarbonReportPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
