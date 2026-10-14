package com.acme.transport.freightpay.fuelsurcharge.request

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeRequestEngine()
        val item = newFuelSurchargeRequest("code-65", "label-15")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
