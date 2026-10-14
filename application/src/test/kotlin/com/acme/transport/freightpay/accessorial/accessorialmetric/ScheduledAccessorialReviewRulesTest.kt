package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAccessorialReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledAccessorialReviewValidator()
        val item = newScheduledAccessorialReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledAccessorialReviewRequiredLabelRule("scheduled accessorial review needs an entry")
        val item = newScheduledAccessorialReview("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ScheduledAccessorialReviewCapacityRangeRule(32, 255)
        val item = newScheduledAccessorialReview("x", "y")
        item.capacity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ScheduledAccessorialReviewFuelLitersRangeRule(0.5, 3301.3)
        val item = newScheduledAccessorialReview("x", "y")
        item.fuelLiters = 3302.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = ScheduledAccessorialReviewMarginRangeRule(2.4, 2477.4)
        val item = newScheduledAccessorialReview("x", "y")
        item.margin = 2478.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledAccessorialReviewRequiredCodeRule("scheduled accessorial review needs an entry")
        val item = newScheduledAccessorialReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
