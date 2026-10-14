package com.acme.transport.freightpay.fuelsurcharge.request

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeVarianceEngine()
        val item = newFuelSurchargeVariance("code-122", "label-610")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
