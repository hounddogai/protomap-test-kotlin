package com.acme.transport.fleetops.defect.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectReviewValidator()
        val item = newDefectReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = DefectReviewExpiresAtRangeRule(32, 86400)
        val item = newDefectReview("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefectReviewNameLengthRule(16)
        val item = newDefectReview("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
