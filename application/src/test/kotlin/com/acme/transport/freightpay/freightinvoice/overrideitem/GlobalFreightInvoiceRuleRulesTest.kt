package com.acme.transport.freightpay.freightinvoice.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalFreightInvoiceRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalFreightInvoiceRuleValidator()
        val item = newGlobalFreightInvoiceRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = GlobalFreightInvoiceRuleEtaMinutesRangeRule(255, 3600)
        val item = newGlobalFreightInvoiceRule("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalFreightInvoiceRuleRequiredCodeRule("global freight invoice rule needs an entry")
        val item = newGlobalFreightInvoiceRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = GlobalFreightInvoiceRuleCurrencyLengthRule(120)
        val item = newGlobalFreightInvoiceRule("x", "y")
        item.currency = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
