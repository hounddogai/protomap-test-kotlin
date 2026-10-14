package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportBudgetEngine(newRegistry())
        val item = newCarbonReportBudget("code-64", "name-849")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
