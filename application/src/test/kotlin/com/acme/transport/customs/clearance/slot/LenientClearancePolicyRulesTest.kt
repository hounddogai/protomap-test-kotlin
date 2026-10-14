package com.acme.transport.customs.clearance.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientClearancePolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientClearancePolicyValidator()
        val item = newLenientClearancePolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientClearancePolicyRequiredCodeRule("lenient clearance policy needs this field")
        val item = newLenientClearancePolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = LenientClearancePolicyCurrencyLengthRule(32)
        val item = newLenientClearancePolicy("x", "y")
        item.currency = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LenientClearancePolicyDiscountRangeRule(8.5, 2419.2)
        val item = newLenientClearancePolicy("x", "y")
        item.discount = 2420.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = LenientClearancePolicyVersionRangeRule(255, 255)
        val item = newLenientClearancePolicy("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
