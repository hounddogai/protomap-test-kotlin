package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeScoreEngine(newRegistry())
        val item = newDisputeScore("code-373", "title-284")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
