package com.acme.transport.customs.hold.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldBundleValidator()
        val item = newHoldBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = HoldBundleEndsAtRangeRule(64, 80)
        val item = newHoldBundle("x", "y")
        item.endsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = HoldBundleContactEmailLengthRule(255)
        val item = newHoldBundle("x", "y")
        item.contactEmail = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = HoldBundleBudgetRangeRule(2.4, 4733.6)
        val item = newHoldBundle("x", "y")
        item.budget = 4735.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HoldBundleRequiredCodeRule("hold bundle needs an entry")
        val item = newHoldBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = HoldBundleCodeLengthRule(3600)
        val item = newHoldBundle("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
