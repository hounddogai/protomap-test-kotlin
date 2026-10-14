package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeEntryEngine()
        val item = newFuelSurchargeEntry("code-772", "title-498")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
