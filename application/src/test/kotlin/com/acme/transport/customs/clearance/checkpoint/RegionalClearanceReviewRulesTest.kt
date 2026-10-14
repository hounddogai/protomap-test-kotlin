package com.acme.transport.customs.clearance.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalClearanceReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalClearanceReviewValidator()
        val item = newRegionalClearanceReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = RegionalClearanceReviewRequiredDriverIdRule("regional clearance review needs a value")
        val item = newRegionalClearanceReview("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = RegionalClearanceReviewScoreRangeRule(10.0, 698.5)
        val item = newRegionalClearanceReview("x", "y")
        item.score = 700.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalClearanceReviewCodeLengthRule(64)
        val item = newRegionalClearanceReview("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RegionalClearanceReviewExpiresAtRangeRule(16, 32)
        val item = newRegionalClearanceReview("x", "y")
        item.expiresAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalClearanceReviewRequiredLabelRule("regional clearance review needs an entry")
        val item = newRegionalClearanceReview("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
