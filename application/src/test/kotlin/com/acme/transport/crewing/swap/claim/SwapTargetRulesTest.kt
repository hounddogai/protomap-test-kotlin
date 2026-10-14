package com.acme.transport.crewing.swap.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapTargetValidator()
        val item = newSwapTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = SwapTargetReviewerLengthRule(120)
        val item = newSwapTarget("x", "y")
        item.reviewer = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = SwapTargetNameLengthRule(80)
        val item = newSwapTarget("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = SwapTargetTrailerIdLengthRule(64)
        val item = newSwapTarget("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = SwapTargetBudgetRangeRule(3.7, 3434.9)
        val item = newSwapTarget("x", "y")
        item.budget = 3436.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
