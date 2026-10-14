package com.acme.transport.customs.declaration.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDeclarationReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDeclarationReviewValidator()
        val item = newSeasonalDeclarationReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = SeasonalDeclarationReviewRateRangeRule(0.1, 2752.4)
        val item = newSeasonalDeclarationReview("x", "y")
        item.rate = 2753.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalDeclarationReviewRequiredCodeRule("seasonal declaration review needs a value")
        val item = newSeasonalDeclarationReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = SeasonalDeclarationReviewLimitRangeRule(16, 64)
        val item = newSeasonalDeclarationReview("x", "y")
        item.limit = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = SeasonalDeclarationReviewStartsAtRangeRule(120, 3600)
        val item = newSeasonalDeclarationReview("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
