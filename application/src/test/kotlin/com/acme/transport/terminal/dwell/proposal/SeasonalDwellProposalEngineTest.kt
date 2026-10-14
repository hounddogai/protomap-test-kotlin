package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDwellProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDwellProposalEngine(newRegistry())
        val item = newSeasonalDwellProposal("code-54", "title-58")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
