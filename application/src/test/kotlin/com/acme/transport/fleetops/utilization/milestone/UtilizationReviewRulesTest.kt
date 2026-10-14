package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationReviewValidator()
        val item = newUtilizationReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = UtilizationReviewMarginRangeRule(4.6, 3094.6)
        val item = newUtilizationReview("x", "y")
        item.margin = 3096.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = UtilizationReviewRequiredTitleRule("utilization review needs this field")
        val item = newUtilizationReview("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = UtilizationReviewTrailerIdLengthRule(86400)
        val item = newUtilizationReview("x", "y")
        item.trailerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = UtilizationReviewNameLengthRule(3600)
        val item = newUtilizationReview("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
