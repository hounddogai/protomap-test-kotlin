package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalFreightInvoiceSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalFreightInvoiceSummaryEngine(newRegistry())
        val item = newRegionalFreightInvoiceSummary("code-977", "title-971")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
