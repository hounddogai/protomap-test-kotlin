package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectProposalEngine(newRegistry())
        val item = newDefectProposal("code-175", "name-168")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
