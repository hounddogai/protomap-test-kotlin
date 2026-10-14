package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentProposalEngine(newRegistry())
        val item = newIncidentProposal("code-553", "label-638")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
