package com.acme.transport.linehaul.capacity.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientCapacityProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientCapacityProposalEngine()
        val item = newLenientCapacityProposal("code-451", "title-431")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
