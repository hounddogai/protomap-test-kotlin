package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakProposalEngine(newRegistry())
        val item = newRestBreakProposal("code-122", "label-989")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
