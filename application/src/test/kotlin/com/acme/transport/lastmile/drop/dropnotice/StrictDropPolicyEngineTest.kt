package com.acme.transport.lastmile.drop.dropnotice

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictDropPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictDropPolicyEngine()
        val item = newStrictDropPolicy("code-368", "name-895")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
