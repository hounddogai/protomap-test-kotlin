package com.acme.transport.linehaul.tender.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackTenderPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackTenderPlanEngine()
        val item = newFallbackTenderPlan("code-806", "name-239")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
