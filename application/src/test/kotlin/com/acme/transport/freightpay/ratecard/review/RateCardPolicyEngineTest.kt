package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardPolicyEngine(newRegistry())
        val item = newRateCardPolicy("code-654", "title-367")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
