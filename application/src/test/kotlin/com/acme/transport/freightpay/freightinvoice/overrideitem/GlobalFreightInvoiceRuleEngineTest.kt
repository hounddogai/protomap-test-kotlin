package com.acme.transport.freightpay.freightinvoice.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalFreightInvoiceRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalFreightInvoiceRuleEngine()
        val item = newGlobalFreightInvoiceRule("code-101", "name-631")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
