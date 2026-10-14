package com.acme.transport.freightpay.freightinvoice.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFreightInvoiceRuleEngine()
        val item = newFreightInvoiceRule("code-888", "name-174")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
