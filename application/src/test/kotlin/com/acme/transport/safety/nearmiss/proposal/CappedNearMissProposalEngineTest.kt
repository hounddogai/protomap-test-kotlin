package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedNearMissProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedNearMissProposalEngine(newRegistry())
        val item = newCappedNearMissProposal("code-788", "label-220")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
