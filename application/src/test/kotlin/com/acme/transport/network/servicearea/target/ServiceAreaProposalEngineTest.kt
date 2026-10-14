package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaProposalEngine(newRegistry())
        val item = newServiceAreaProposal("code-227", "label-333")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
