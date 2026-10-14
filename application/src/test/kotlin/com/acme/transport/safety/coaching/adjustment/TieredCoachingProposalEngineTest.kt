package com.acme.transport.safety.coaching.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredCoachingProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredCoachingProposalEngine()
        val item = newTieredCoachingProposal("code-78", "name-55")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
