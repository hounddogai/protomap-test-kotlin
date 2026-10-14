package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoicePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoicePolicyEngine(newRegistry())
        val item = newFreightInvoicePolicy("code-517", "label-993")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
