package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalAccessorialProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalAccessorialProposalEngine(newRegistry())
        val item = newSeasonalAccessorialProposal("code-682", "label-477")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
