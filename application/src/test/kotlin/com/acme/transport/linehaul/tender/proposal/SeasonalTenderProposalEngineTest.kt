package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalTenderProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalTenderProposalEngine(newRegistry())
        val item = newSeasonalTenderProposal("code-82", "label-577")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
