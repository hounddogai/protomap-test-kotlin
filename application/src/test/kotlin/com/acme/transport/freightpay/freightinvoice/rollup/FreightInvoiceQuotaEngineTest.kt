package com.acme.transport.freightpay.freightinvoice.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceQuotaEngine()
        val item = newFreightInvoiceQuota("code-961", "label-848")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
