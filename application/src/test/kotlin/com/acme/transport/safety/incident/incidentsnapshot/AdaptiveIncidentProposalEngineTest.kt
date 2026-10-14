package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveIncidentProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveIncidentProposalEngine(newRegistry())
        val item = newAdaptiveIncidentProposal("code-315", "label-211")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
