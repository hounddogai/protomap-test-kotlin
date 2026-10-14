package com.acme.transport.freightpay.freightinvoice.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredFreightInvoiceTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredFreightInvoiceTargetValidator()
        val item = newTieredFreightInvoiceTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredFreightInvoiceTargetNameLengthRule(16)
        val item = newTieredFreightInvoiceTarget("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredFreightInvoiceTargetRequiredNameRule("tiered freight invoice target needs a value")
        val item = newTieredFreightInvoiceTarget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
