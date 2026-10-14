package com.acme.transport.fleetops.tire.tirebudget

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTireBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTireBudgetEngine()
        val item = newManualTireBudget("code-533", "name-413")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
