package com.acme.transport.freightpay.ratecard.request

import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardAgreementEngine()
        val item = newRateCardAgreement("code-17", "name-238")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
