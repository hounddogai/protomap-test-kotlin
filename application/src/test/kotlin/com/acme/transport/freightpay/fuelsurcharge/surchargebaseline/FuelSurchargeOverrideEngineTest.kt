package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeOverrideEngine(newRegistry())
        val item = newFuelSurchargeOverride("code-483", "title-741")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
