package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardTierEngine(newRegistry())
        val item = newRateCardTier("code-757", "title-926")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
