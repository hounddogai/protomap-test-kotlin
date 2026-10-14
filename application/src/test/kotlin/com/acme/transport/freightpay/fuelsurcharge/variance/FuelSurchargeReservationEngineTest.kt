package com.acme.transport.freightpay.fuelsurcharge.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeReservationEngine(newRegistry())
        val item = newFuelSurchargeReservation("code-730", "name-637")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
