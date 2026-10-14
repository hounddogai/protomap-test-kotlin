package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledFuelSurchargeWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledFuelSurchargeWindowEngine()
        val item = newScheduledFuelSurchargeWindow("code-894", "name-875")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
