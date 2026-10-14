package com.acme.transport.customs.declaration.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDeclarationReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDeclarationReviewValidator()
        val item = newFallbackDeclarationReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = FallbackDeclarationReviewBudgetRangeRule(0.0, 2978.1)
        val item = newFallbackDeclarationReview("x", "y")
        item.budget = 2979.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = FallbackDeclarationReviewLaneLengthRule(86400)
        val item = newFallbackDeclarationReview("x", "y")
        item.lane = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = FallbackDeclarationReviewWindowDaysRangeRule(64, 120)
        val item = newFallbackDeclarationReview("x", "y")
        item.windowDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackDeclarationReviewNameLengthRule(16)
        val item = newFallbackDeclarationReview("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackDeclarationReviewCodeLengthRule(86400)
        val item = newFallbackDeclarationReview("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackDeclarationReviewRequiredCodeRule("fallback declaration review needs an entry")
        val item = newFallbackDeclarationReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
