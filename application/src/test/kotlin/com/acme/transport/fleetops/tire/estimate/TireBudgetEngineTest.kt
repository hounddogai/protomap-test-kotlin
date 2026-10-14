package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class TireBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireBudgetEngine()
        val item = newTireBudget("code-593", "title-558")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
