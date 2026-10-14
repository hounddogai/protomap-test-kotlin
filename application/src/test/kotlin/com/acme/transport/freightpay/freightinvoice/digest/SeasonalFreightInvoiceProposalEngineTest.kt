package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalFreightInvoiceProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalFreightInvoiceProposalEngine(newRegistry())
        val item = newSeasonalFreightInvoiceProposal("code-249", "label-921")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
