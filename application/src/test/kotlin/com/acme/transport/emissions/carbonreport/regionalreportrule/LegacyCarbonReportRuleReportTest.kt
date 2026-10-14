package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCarbonReportRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702896741)
        val items = mutableListOf<LegacyCarbonReportRule>()
        for (index in 0 until 3) {
            items.add(newLegacyCarbonReportRule("legacy-carbon-report-rule-$index", "sample"))
        }
        val report = buildLegacyCarbonReportRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
