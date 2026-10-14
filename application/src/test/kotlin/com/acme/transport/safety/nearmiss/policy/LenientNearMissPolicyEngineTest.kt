package com.acme.transport.safety.nearmiss.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientNearMissPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientNearMissPolicyEngine()
        val item = newLenientNearMissPolicy("code-358", "name-78")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
