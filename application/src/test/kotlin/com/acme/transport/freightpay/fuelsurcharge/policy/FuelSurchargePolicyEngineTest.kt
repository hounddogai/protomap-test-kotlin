package com.acme.transport.freightpay.fuelsurcharge.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargePolicyEngine()
        val item = newFuelSurchargePolicy("code-745", "title-470")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
