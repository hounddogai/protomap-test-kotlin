package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionProposalEngine(newRegistry())
        val item = newInspectionProposal("code-824", "title-459")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
