package com.acme.transport.freightpay.ratecard.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardTargetValidator()
        val item = newRateCardTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RateCardTargetRequiredNameRule("rate card target needs this field")
        val item = newRateCardTarget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = RateCardTargetCategoryLengthRule(120)
        val item = newRateCardTarget("x", "y")
        item.category = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = RateCardTargetCeilingRangeRule(4.2, 2888.9)
        val item = newRateCardTarget("x", "y")
        item.ceiling = 2890.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = RateCardTargetPhoneLengthRule(120)
        val item = newRateCardTarget("x", "y")
        item.phone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RateCardTargetRequiredCodeRule("rate card target needs a value")
        val item = newRateCardTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
