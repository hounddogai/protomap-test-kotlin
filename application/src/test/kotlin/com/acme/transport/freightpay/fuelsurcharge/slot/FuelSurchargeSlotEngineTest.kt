package com.acme.transport.freightpay.fuelsurcharge.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeSlotEngine(newRegistry())
        val item = newFuelSurchargeSlot("code-745", "title-127")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
