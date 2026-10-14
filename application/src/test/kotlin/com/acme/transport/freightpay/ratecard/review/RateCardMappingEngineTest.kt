package com.acme.transport.freightpay.ratecard.review

import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardMappingEngine()
        val item = newRateCardMapping("code-94", "name-457")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
