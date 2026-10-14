package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeAdjustmentEngine(newRegistry())
        val item = newFuelSurchargeAdjustment("code-423", "name-642")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
