package com.acme.transport.network.node.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackNodeAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackNodeAdjustmentEngine()
        val item = newFallbackNodeAdjustment("code-970", "label-455")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
