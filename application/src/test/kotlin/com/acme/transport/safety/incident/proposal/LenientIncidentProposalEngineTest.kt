package com.acme.transport.safety.incident.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientIncidentProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientIncidentProposalEngine()
        val item = newLenientIncidentProposal("code-969", "name-749")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
