package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRateCardMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRateCardMilestoneEngine()
        val item = newStrictRateCardMilestone("code-619", "label-413")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
