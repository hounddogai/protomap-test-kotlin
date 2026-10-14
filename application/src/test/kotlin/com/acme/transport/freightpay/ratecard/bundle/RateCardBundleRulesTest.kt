package com.acme.transport.freightpay.ratecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardBundleValidator()
        val item = newRateCardBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = RateCardBundleCapacityRangeRule(64, 120)
        val item = newRateCardBundle("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RateCardBundleRequiredNameRule("rate card bundle needs a value")
        val item = newRateCardBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RateCardBundleNameLengthRule(120)
        val item = newRateCardBundle("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = RateCardBundleRequiredCategoryRule("rate card bundle needs an entry")
        val item = newRateCardBundle("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RateCardBundleSequenceRangeRule(255, 3600)
        val item = newRateCardBundle("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = RateCardBundleDiscountRangeRule(9.2, 225.2)
        val item = newRateCardBundle("x", "y")
        item.discount = 226.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
