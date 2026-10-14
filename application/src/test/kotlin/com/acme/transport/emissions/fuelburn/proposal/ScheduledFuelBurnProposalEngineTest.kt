package com.acme.transport.emissions.fuelburn.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledFuelBurnProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledFuelBurnProposalEngine(newRegistry())
        val item = newScheduledFuelBurnProposal("code-980", "name-615")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
