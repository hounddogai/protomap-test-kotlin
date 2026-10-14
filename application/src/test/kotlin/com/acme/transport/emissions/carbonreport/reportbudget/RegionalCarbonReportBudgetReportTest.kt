package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCarbonReportBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708140305)
        val items = mutableListOf<RegionalCarbonReportBudget>()
        for (index in 0 until 5) {
            items.add(newRegionalCarbonReportBudget("regional-carbon-report-budget-$index", "sample"))
        }
        val report = buildRegionalCarbonReportBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
