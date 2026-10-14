package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyRedeliveryProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyRedeliveryProposalEngine(newRegistry())
        val item = newLegacyRedeliveryProposal("code-315", "title-938")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
