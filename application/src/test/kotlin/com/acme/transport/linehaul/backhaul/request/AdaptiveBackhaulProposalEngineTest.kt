package com.acme.transport.linehaul.backhaul.request

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveBackhaulProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveBackhaulProposalEngine()
        val item = newAdaptiveBackhaulProposal("code-694", "title-793")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
