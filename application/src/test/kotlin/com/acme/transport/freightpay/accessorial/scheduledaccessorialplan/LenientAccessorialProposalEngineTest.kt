package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientAccessorialProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientAccessorialProposalEngine(newRegistry())
        val item = newLenientAccessorialProposal("code-374", "name-766")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
