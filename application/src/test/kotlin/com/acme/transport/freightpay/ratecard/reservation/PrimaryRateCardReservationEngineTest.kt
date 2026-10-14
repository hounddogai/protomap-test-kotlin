package com.acme.transport.freightpay.ratecard.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryRateCardReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryRateCardReservationEngine()
        val item = newPrimaryRateCardReservation("code-681", "label-231")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
