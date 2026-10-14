package com.acme.transport.crewing.availability.weightedavailabilityreservation

import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityProposalEngine()
        val item = newAvailabilityProposal("code-243", "title-399")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
