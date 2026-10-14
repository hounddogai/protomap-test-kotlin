package com.acme.transport.emissions.idling.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingIdlingProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingIdlingProposalEngine()
        val item = newRollingIdlingProposal("code-881", "name-250")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
