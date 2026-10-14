package com.acme.transport.customs.clearance.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class ClearancePlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearancePlanEngine()
        val item = newClearancePlan("code-864", "label-349")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
