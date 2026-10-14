package com.acme.transport.freightpay.fuelsurcharge.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeGroupEngine()
        val item = newFuelSurchargeGroup("code-662", "title-884")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
