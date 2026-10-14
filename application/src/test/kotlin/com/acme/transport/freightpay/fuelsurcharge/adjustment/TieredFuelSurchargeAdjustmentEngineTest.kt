package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredFuelSurchargeAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredFuelSurchargeAdjustmentEngine(newRegistry())
        val item = newTieredFuelSurchargeAdjustment("code-913", "name-420")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
