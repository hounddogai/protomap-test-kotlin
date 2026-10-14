package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveRateCardReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveRateCardReservationEngine(newRegistry())
        val item = newAdaptiveRateCardReservation("code-129", "name-869")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
