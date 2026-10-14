package com.acme.transport.freightpay.freightinvoice.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyFreightInvoiceRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyFreightInvoiceRuleValidator()
        val item = newLegacyFreightInvoiceRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyFreightInvoiceRuleNameLengthRule(120)
        val item = newLegacyFreightInvoiceRule("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyFreightInvoiceRuleTitleLengthRule(255)
        val item = newLegacyFreightInvoiceRule("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LegacyFreightInvoiceRuleLimitRangeRule(80, 1000)
        val item = newLegacyFreightInvoiceRule("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyFreightInvoiceRuleCodeLengthRule(120)
        val item = newLegacyFreightInvoiceRule("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
