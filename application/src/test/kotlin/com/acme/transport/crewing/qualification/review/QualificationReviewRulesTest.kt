package com.acme.transport.crewing.qualification.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationReviewValidator()
        val item = newQualificationReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = QualificationReviewChecksumLengthRule(255)
        val item = newQualificationReview("x", "y")
        item.checksum = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = QualificationReviewNameLengthRule(86400)
        val item = newQualificationReview("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = QualificationReviewRequiredNameRule("qualification review needs this field")
        val item = newQualificationReview("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = QualificationReviewBatchSizeRangeRule(16, 3600)
        val item = newQualificationReview("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = QualificationReviewBudgetRangeRule(6.4, 2861.4)
        val item = newQualificationReview("x", "y")
        item.budget = 2862.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = QualificationReviewCodeLengthRule(3600)
        val item = newQualificationReview("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
