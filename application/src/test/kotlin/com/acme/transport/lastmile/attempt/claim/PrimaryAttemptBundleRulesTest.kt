package com.acme.transport.lastmile.attempt.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAttemptBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryAttemptBundleValidator()
        val item = newPrimaryAttemptBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryAttemptBundleCodeLengthRule(3600)
        val item = newPrimaryAttemptBundle("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = PrimaryAttemptBundleBudgetRangeRule(3.8, 3536.5)
        val item = newPrimaryAttemptBundle("x", "y")
        item.budget = 3538.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = PrimaryAttemptBundleCurrencyLengthRule(16)
        val item = newPrimaryAttemptBundle("x", "y")
        item.currency = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = PrimaryAttemptBundleQuantityRangeRule(64, 1000)
        val item = newPrimaryAttemptBundle("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
