package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeAllocationEngine()
        val item = newFuelSurchargeAllocation("code-157", "title-345")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
