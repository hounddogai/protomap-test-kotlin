package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredFuelSurchargeBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredFuelSurchargeBaselineEngine()
        val item = newTieredFuelSurchargeBaseline("code-230", "label-561")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
