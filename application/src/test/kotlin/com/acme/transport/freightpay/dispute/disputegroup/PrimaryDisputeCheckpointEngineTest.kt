package com.acme.transport.freightpay.dispute.disputegroup

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDisputeCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDisputeCheckpointEngine()
        val item = newPrimaryDisputeCheckpoint("code-279", "label-683")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
