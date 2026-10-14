package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDockDoorProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDockDoorProposalEngine(newRegistry())
        val item = newRegionalDockDoorProposal("code-430", "name-8")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
