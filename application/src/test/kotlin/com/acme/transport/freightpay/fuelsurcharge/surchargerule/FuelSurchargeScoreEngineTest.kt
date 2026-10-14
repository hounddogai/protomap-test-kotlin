package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeScoreEngine(newRegistry())
        val item = newFuelSurchargeScore("code-423", "name-166")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
