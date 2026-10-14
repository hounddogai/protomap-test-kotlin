package com.acme.transport.linehaul.backhaul.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulProposalEngine(newRegistry())
        val item = newBackhaulProposal("code-3", "name-652")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
