package com.acme.transport.customs.hold.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HoldProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldProposalEngine(newRegistry())
        val item = newHoldProposal("code-424", "title-164")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
