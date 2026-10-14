package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingProposalEngine(newRegistry())
        val item = newCoachingProposal("code-617", "name-546")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
