package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeProposalEngine(newRegistry())
        val item = newEvChargeProposal("code-796", "label-477")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
