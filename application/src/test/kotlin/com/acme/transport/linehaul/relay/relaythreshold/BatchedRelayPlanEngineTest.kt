package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRelayPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRelayPlanEngine(newRegistry())
        val item = newBatchedRelayPlan("code-403", "label-696")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
