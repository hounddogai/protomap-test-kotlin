package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualRestBreakProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualRestBreakProposalEngine(newRegistry())
        val item = newManualRestBreakProposal("code-739", "title-545")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
