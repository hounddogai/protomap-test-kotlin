package com.acme.transport.freightpay.fuelsurcharge.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeCycleEngine()
        val item = newFuelSurchargeCycle("code-163", "name-657")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
