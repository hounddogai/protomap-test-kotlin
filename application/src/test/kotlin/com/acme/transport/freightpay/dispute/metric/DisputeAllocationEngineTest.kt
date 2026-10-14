package com.acme.transport.freightpay.dispute.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeAllocationEngine()
        val item = newDisputeAllocation("code-977", "name-794")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
