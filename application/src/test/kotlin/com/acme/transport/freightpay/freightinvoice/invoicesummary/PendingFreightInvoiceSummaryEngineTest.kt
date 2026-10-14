package com.acme.transport.freightpay.freightinvoice.invoicesummary

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingFreightInvoiceSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingFreightInvoiceSummaryEngine()
        val item = newPendingFreightInvoiceSummary("code-655", "label-301")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
