package com.acme.transport.freightpay.accessorial.accessorialadjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveAccessorialAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveAccessorialAdjustmentEngine()
        val item = newAdaptiveAccessorialAdjustment("code-990", "label-565")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
