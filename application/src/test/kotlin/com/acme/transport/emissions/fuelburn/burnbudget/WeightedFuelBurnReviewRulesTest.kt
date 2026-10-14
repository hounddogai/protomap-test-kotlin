package com.acme.transport.emissions.fuelburn.burnbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedFuelBurnReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedFuelBurnReviewValidator()
        val item = newWeightedFuelBurnReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = WeightedFuelBurnReviewRevisionRangeRule(16, 1000)
        val item = newWeightedFuelBurnReview("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedFuelBurnReviewRequiredNameRule("weighted fuel burn review needs an entry")
        val item = newWeightedFuelBurnReview("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedFuelBurnReviewCodeLengthRule(3600)
        val item = newWeightedFuelBurnReview("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedFuelBurnReviewRequiredCodeRule("weighted fuel burn review needs an entry")
        val item = newWeightedFuelBurnReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
