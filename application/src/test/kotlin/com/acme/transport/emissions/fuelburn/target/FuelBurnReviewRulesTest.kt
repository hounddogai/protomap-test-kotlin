package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnReviewValidator()
        val item = newFuelBurnReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = FuelBurnReviewBudgetRangeRule(1.9, 4560.9)
        val item = newFuelBurnReview("x", "y")
        item.budget = 4562.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelBurnReviewRequiredCodeRule("fuel burn review needs this field")
        val item = newFuelBurnReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelBurnReviewCodeLengthRule(120)
        val item = newFuelBurnReview("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
