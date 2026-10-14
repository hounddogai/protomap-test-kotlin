package com.acme.transport.freightpay.ratecard.cardrequest

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredRateCardRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredRateCardRequestEngine()
        val item = newTieredRateCardRequest("code-395", "name-701")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
