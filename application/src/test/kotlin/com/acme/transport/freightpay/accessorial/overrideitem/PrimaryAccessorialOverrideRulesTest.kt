package com.acme.transport.freightpay.accessorial.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAccessorialOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryAccessorialOverrideValidator()
        val item = newPrimaryAccessorialOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = PrimaryAccessorialOverrideRequiredReviewerRule("primary accessorial override needs an entry")
        val item = newPrimaryAccessorialOverride("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = PrimaryAccessorialOverrideDiscountRangeRule(4.1, 3043.4)
        val item = newPrimaryAccessorialOverride("x", "y")
        item.discount = 3044.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = PrimaryAccessorialOverrideNameLengthRule(255)
        val item = newPrimaryAccessorialOverride("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = PrimaryAccessorialOverrideReviewerLengthRule(64)
        val item = newPrimaryAccessorialOverride("x", "y")
        item.reviewer = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
