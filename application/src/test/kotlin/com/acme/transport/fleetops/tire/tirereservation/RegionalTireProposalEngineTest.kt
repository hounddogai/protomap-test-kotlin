package com.acme.transport.fleetops.tire.tirereservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalTireProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalTireProposalEngine(newRegistry())
        val item = newRegionalTireProposal("code-189", "name-519")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
