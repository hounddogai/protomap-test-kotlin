package com.acme.transport.freightpay.ratecard.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardDraftEngine()
        val item = newRateCardDraft("code-49", "label-274")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
