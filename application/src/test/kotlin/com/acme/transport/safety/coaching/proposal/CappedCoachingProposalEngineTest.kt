package com.acme.transport.safety.coaching.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedCoachingProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedCoachingProposalEngine()
        val item = newCappedCoachingProposal("code-738", "label-583")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
