package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockProposalEngine(newRegistry())
        val item = newCrossDockProposal("code-535", "name-775")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
