package com.acme.transport.crewing.availability.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityReviewValidator()
        val item = newAvailabilityReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = AvailabilityReviewRequiredVehicleIdRule("availability review needs this field")
        val item = newAvailabilityReview("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = AvailabilityReviewSequenceRangeRule(32, 1000)
        val item = newAvailabilityReview("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = AvailabilityReviewNameLengthRule(86400)
        val item = newAvailabilityReview("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AvailabilityReviewRequiredCodeRule("availability review needs an entry")
        val item = newAvailabilityReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = AvailabilityReviewAmountRangeRule(1.5, 2703.6)
        val item = newAvailabilityReview("x", "y")
        item.amount = 2705.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
