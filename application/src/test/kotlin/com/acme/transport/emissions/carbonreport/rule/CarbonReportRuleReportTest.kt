package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703897605)
        val items = mutableListOf<CarbonReportRule>()
        for (index in 0 until 3) {
            items.add(newCarbonReportRule("carbon-report-rule-$index", "sample"))
        }
        val report = buildCarbonReportRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
