package com.acme.transport.freightpay.freightinvoice.invoicequota

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingFreightInvoiceQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingFreightInvoiceQuotaEngine()
        val item = newRollingFreightInvoiceQuota("code-774", "name-850")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
