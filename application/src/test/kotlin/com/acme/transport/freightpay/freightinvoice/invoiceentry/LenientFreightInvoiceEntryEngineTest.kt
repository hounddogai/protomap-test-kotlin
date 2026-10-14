package com.acme.transport.freightpay.freightinvoice.invoiceentry

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientFreightInvoiceEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientFreightInvoiceEntryEngine()
        val item = newLenientFreightInvoiceEntry("code-658", "name-851")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
