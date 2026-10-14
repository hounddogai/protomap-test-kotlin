package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingFreightInvoiceRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingFreightInvoiceRequestEngine()
        val item = newRollingFreightInvoiceRequest("code-855", "title-429")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
