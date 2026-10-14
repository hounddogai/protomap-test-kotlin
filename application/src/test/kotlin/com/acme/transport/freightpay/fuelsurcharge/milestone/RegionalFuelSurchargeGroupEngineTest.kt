package com.acme.transport.freightpay.fuelsurcharge.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalFuelSurchargeGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalFuelSurchargeGroupEngine()
        val item = newRegionalFuelSurchargeGroup("code-844", "label-229")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
