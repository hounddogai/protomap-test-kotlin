package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargePreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargePreferenceEngine(newRegistry())
        val item = newFuelSurchargePreference("code-145", "title-203")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
