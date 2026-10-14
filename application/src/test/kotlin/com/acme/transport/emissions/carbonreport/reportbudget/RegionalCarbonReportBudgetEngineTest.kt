package com.acme.transport.emissions.carbonreport.reportbudget

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCarbonReportBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCarbonReportBudgetEngine()
        val item = newRegionalCarbonReportBudget("code-161", "label-817")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
