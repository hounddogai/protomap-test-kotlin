package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireProposalEngine(newRegistry())
        val item = newTireProposal("code-268", "label-673")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
