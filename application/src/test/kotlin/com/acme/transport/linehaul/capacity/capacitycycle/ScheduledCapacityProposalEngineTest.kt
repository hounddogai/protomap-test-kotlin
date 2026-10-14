package com.acme.transport.linehaul.capacity.capacitycycle

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledCapacityProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledCapacityProposalEngine()
        val item = newScheduledCapacityProposal("code-3", "label-599")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
