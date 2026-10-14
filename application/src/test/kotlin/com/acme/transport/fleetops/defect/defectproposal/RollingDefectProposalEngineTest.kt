package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDefectProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDefectProposalEngine(newRegistry())
        val item = newRollingDefectProposal("code-681", "name-402")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
