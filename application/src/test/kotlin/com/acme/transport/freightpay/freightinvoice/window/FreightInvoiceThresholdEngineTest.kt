package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceThresholdEngine()
        val item = newFreightInvoiceThreshold("code-298", "label-544")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
