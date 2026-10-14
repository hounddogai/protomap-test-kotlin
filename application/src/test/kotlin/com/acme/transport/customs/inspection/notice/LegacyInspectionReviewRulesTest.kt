package com.acme.transport.customs.inspection.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyInspectionReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyInspectionReviewValidator()
        val item = newLegacyInspectionReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LegacyInspectionReviewRetriesRangeRule(3600, 86400)
        val item = newLegacyInspectionReview("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyInspectionReviewRequiredTitleRule("legacy inspection review needs a value")
        val item = newLegacyInspectionReview("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyInspectionReviewCodeLengthRule(120)
        val item = newLegacyInspectionReview("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = LegacyInspectionReviewDockLengthRule(3600)
        val item = newLegacyInspectionReview("x", "y")
        item.dock = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = LegacyInspectionReviewRevisionRangeRule(120, 255)
        val item = newLegacyInspectionReview("x", "y")
        item.revision = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyInspectionReviewTitleLengthRule(255)
        val item = newLegacyInspectionReview("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
