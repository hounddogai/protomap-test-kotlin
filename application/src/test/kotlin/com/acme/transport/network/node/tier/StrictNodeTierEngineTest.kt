package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictNodeTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictNodeTierEngine()
        val item = newStrictNodeTier("code-949", "title-280")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
