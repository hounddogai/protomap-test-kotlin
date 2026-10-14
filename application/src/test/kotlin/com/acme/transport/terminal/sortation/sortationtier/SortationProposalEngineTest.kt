package com.acme.transport.terminal.sortation.sortationtier

import kotlin.test.Test
import kotlin.test.assertTrue

class SortationProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationProposalEngine()
        val item = newSortationProposal("code-221", "title-779")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
