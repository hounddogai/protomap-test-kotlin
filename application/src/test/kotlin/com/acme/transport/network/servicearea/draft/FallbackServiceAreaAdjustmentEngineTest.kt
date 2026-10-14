package com.acme.transport.network.servicearea.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackServiceAreaAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackServiceAreaAdjustmentEngine()
        val item = newFallbackServiceAreaAdjustment("code-41", "name-216")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
