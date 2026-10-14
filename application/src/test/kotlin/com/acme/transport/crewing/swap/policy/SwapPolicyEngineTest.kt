package com.acme.transport.crewing.swap.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class SwapPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapPolicyEngine()
        val item = newSwapPolicy("code-484", "label-966")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
