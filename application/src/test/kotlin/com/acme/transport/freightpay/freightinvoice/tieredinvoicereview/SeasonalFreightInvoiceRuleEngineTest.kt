package com.acme.transport.freightpay.freightinvoice.tieredinvoicereview

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalFreightInvoiceRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalFreightInvoiceRuleEngine(newRegistry())
        val item = newSeasonalFreightInvoiceRule("code-157", "name-248")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
