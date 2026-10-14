package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRateCardMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRateCardMilestoneEngine(newRegistry())
        val item = newBatchedRateCardMilestone("code-896", "title-956")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
