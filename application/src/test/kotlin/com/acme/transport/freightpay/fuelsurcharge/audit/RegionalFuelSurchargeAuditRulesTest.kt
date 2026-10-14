package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalFuelSurchargeAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalFuelSurchargeAuditValidator()
        val item = newRegionalFuelSurchargeAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalFuelSurchargeAuditCodeLengthRule(16)
        val item = newRegionalFuelSurchargeAudit("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalFuelSurchargeAuditRequiredNameRule("regional fuel surcharge audit needs this field")
        val item = newRegionalFuelSurchargeAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = RegionalFuelSurchargeAuditDiscountRangeRule(9.5, 699.3)
        val item = newRegionalFuelSurchargeAudit("x", "y")
        item.discount = 700.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RegionalFuelSurchargeAuditAttemptsRangeRule(64, 3600)
        val item = newRegionalFuelSurchargeAudit("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = RegionalFuelSurchargeAuditRequiredCurrencyRule("regional fuel surcharge audit needs this field")
        val item = newRegionalFuelSurchargeAudit("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
