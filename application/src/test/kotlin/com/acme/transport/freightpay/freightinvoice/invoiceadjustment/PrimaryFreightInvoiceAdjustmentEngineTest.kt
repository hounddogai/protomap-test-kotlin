package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryFreightInvoiceAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryFreightInvoiceAdjustmentEngine(newRegistry())
        val item = newPrimaryFreightInvoiceAdjustment("code-923", "label-420")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
