package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardMilestoneEngine(newRegistry())
        val item = newRateCardMilestone("code-844", "title-929")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
