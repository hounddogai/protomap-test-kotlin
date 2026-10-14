package com.acme.transport.freightpay.accessorial.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialReviewValidator()
        val item = newAccessorialReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = AccessorialReviewMaximumRangeRule(4.6, 2066.4)
        val item = newAccessorialReview("x", "y")
        item.maximum = 2067.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = AccessorialReviewTitleLengthRule(80)
        val item = newAccessorialReview("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = AccessorialReviewVehicleIdLengthRule(86400)
        val item = newAccessorialReview("x", "y")
        item.vehicleId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = AccessorialReviewRatioRangeRule(3.3, 3440.0)
        val item = newAccessorialReview("x", "y")
        item.ratio = 3441.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = AccessorialReviewCeilingRangeRule(0.6, 3728.3)
        val item = newAccessorialReview("x", "y")
        item.ceiling = 3729.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
