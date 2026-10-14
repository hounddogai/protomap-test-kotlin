package com.acme.transport.freightpay.ratecard.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardNoticeEngine()
        val item = newRateCardNotice("code-349", "name-709")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
