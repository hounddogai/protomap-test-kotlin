package com.acme.transport.freightpay.fuelsurcharge.window

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalFuelSurchargeBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalFuelSurchargeBaselineEngine()
        val item = newGlobalFuelSurchargeBaseline("code-689", "title-449")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
