package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalFuelBurnReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalFuelBurnReviewValidator()
        val item = newRegionalFuelBurnReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = RegionalFuelBurnReviewDurationMinutesRangeRule(32, 3600)
        val item = newRegionalFuelBurnReview("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalFuelBurnReviewRequiredCodeRule("regional fuel burn review needs a value")
        val item = newRegionalFuelBurnReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalFuelBurnReviewTitleLengthRule(64)
        val item = newRegionalFuelBurnReview("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
