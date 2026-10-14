package com.acme.transport.emissions.carbonreport.score

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveCarbonReportProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveCarbonReportProposalEngine()
        val item = newAdaptiveCarbonReportProposal("code-100", "title-567")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
