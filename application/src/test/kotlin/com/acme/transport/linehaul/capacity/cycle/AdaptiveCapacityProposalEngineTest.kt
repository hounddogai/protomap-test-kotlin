package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveCapacityProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveCapacityProposalEngine(newRegistry())
        val item = newAdaptiveCapacityProposal("code-596", "name-567")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
