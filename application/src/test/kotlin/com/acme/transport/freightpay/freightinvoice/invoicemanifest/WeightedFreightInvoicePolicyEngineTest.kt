package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedFreightInvoicePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedFreightInvoicePolicyEngine(newRegistry())
        val item = newWeightedFreightInvoicePolicy("code-983", "label-29")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
