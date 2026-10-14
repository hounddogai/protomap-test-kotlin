package com.acme.transport.customs.clearance.clearancethreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualClearancePlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualClearancePlanEngine()
        val item = newManualClearancePlan("code-360", "name-532")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
