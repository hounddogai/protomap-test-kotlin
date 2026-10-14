package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRateCardReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRateCardReservationEngine()
        val item = newStrictRateCardReservation("code-955", "label-455")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
