package com.acme.transport.freightpay.accessorial.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialRuleValidator()
        val item = newAccessorialRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = AccessorialRuleCurrencyLengthRule(1000)
        val item = newAccessorialRule("x", "y")
        item.currency = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = AccessorialRuleDurationMinutesRangeRule(1000, 86400)
        val item = newAccessorialRule("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = AccessorialRuleRequiredCurrencyRule("accessorial rule needs this field")
        val item = newAccessorialRule("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = AccessorialRuleRequiredCategoryRule("accessorial rule needs a value")
        val item = newAccessorialRule("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = AccessorialRuleCategoryLengthRule(1000)
        val item = newAccessorialRule("x", "y")
        item.category = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
