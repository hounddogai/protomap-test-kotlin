package com.acme.transport.safety.incident.globalincidentproposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultIncidentProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultIncidentProposalEngine(newRegistry())
        val item = newDefaultIncidentProposal("code-938", "label-536")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
