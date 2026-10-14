package com.acme.transport.linehaul.tender.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveTenderProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveTenderProposalEngine()
        val item = newAdaptiveTenderProposal("code-847", "title-163")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
