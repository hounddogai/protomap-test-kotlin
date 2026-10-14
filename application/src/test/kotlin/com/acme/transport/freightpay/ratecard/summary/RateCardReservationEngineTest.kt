package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardReservationEngine(newRegistry())
        val item = newRateCardReservation("code-112", "name-255")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
