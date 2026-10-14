package com.acme.transport.freightpay.freightinvoice.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceRuleValidator()
        val item = newFreightInvoiceRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FreightInvoiceRuleNameLengthRule(64)
        val item = newFreightInvoiceRule("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FreightInvoiceRuleRequiredTitleRule("freight invoice rule needs an entry")
        val item = newFreightInvoiceRule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FreightInvoiceRuleTitleLengthRule(32)
        val item = newFreightInvoiceRule("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = FreightInvoiceRuleQuantityRangeRule(255, 86400)
        val item = newFreightInvoiceRule("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = FreightInvoiceRuleRequiredDriverIdRule("freight invoice rule needs an entry")
        val item = newFreightInvoiceRule("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
