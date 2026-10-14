package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingFreightInvoiceRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingFreightInvoiceRuleEngine()
        val item = newPendingFreightInvoiceRule("code-687", "title-834")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
