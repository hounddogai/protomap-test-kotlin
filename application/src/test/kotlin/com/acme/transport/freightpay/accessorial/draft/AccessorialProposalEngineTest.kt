package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialProposalEngine(newRegistry())
        val item = newAccessorialProposal("code-171", "label-843")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
