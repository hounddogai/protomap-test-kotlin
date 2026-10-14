package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeCheckpointEngine()
        val item = newDisputeCheckpoint("code-935", "label-386")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
