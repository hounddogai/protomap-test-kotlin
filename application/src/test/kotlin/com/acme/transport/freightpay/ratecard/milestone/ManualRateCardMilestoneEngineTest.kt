package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualRateCardMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualRateCardMilestoneEngine(newRegistry())
        val item = newManualRateCardMilestone("code-245", "label-753")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
