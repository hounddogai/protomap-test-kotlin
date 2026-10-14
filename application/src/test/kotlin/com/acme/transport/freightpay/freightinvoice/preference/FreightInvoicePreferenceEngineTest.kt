package com.acme.transport.freightpay.freightinvoice.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoicePreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoicePreferenceEngine()
        val item = newFreightInvoicePreference("code-923", "label-115")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
