package com.acme.transport.freightpay.freightinvoice.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceMetricEngine()
        val item = newFreightInvoiceMetric("code-246", "name-379")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
