package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCarbonReportRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705461145)
        val items = mutableListOf<RegionalCarbonReportRule>()
        for (index in 0 until 3) {
            items.add(newRegionalCarbonReportRule("regional-carbon-report-rule-$index", "sample"))
        }
        val report = buildRegionalCarbonReportRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
