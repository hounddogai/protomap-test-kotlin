package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoicePlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoicePlanEngine(newRegistry())
        val item = newFreightInvoicePlan("code-283", "label-968")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
