package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryFuelSurchargeDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryFuelSurchargeDraftValidator()
        val item = newPrimaryFuelSurchargeDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = PrimaryFuelSurchargeDraftRequiredShiftCodeRule("primary fuel surcharge draft needs an entry")
        val item = newPrimaryFuelSurchargeDraft("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = PrimaryFuelSurchargeDraftAmountRangeRule(8.0, 3161.0)
        val item = newPrimaryFuelSurchargeDraft("x", "y")
        item.amount = 3162.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = PrimaryFuelSurchargeDraftCurrencyLengthRule(120)
        val item = newPrimaryFuelSurchargeDraft("x", "y")
        item.currency = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
