package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedIncidentProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedIncidentProposalEngine(newRegistry())
        val item = newBatchedIncidentProposal("code-60", "label-458")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
