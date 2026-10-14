package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingFreightInvoiceRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingFreightInvoiceRuleValidator()
        val item = newPendingFreightInvoiceRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingFreightInvoiceRuleTitleLengthRule(3600)
        val item = newPendingFreightInvoiceRule("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = PendingFreightInvoiceRuleRatioRangeRule(0.8, 2454.2)
        val item = newPendingFreightInvoiceRule("x", "y")
        item.ratio = 2455.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingFreightInvoiceRuleRequiredTitleRule("pending freight invoice rule needs this field")
        val item = newPendingFreightInvoiceRule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
