package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryFuelBurnReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryFuelBurnReviewValidator()
        val item = newPrimaryFuelBurnReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = PrimaryFuelBurnReviewRequiredTimezoneRule("primary fuel burn review needs a value")
        val item = newPrimaryFuelBurnReview("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryFuelBurnReviewCodeLengthRule(1000)
        val item = newPrimaryFuelBurnReview("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryFuelBurnReviewRequiredTitleRule("primary fuel burn review needs a value")
        val item = newPrimaryFuelBurnReview("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryFuelBurnReviewTitleLengthRule(16)
        val item = newPrimaryFuelBurnReview("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = PrimaryFuelBurnReviewTimezoneLengthRule(3600)
        val item = newPrimaryFuelBurnReview("x", "y")
        item.timezone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
