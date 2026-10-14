package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackClearancePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackClearancePolicyEngine()
        val item = newFallbackClearancePolicy("code-794", "title-993")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
