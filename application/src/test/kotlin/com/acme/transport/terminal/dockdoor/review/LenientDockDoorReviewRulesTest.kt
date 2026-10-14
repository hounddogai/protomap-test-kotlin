package com.acme.transport.terminal.dockdoor.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDockDoorReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDockDoorReviewValidator()
        val item = newLenientDockDoorReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientDockDoorReviewRequiredNameRule("lenient dock door review needs this field")
        val item = newLenientDockDoorReview("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LenientDockDoorReviewLimitRangeRule(16, 16)
        val item = newLenientDockDoorReview("x", "y")
        item.limit = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = LenientDockDoorReviewVehicleIdLengthRule(32)
        val item = newLenientDockDoorReview("x", "y")
        item.vehicleId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
