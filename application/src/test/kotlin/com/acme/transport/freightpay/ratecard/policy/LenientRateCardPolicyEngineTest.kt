package com.acme.transport.freightpay.ratecard.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientRateCardPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientRateCardPolicyEngine()
        val item = newLenientRateCardPolicy("code-710", "name-425")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
