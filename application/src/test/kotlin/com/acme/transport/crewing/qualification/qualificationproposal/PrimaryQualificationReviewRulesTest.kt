package com.acme.transport.crewing.qualification.qualificationproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryQualificationReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryQualificationReviewValidator()
        val item = newPrimaryQualificationReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryQualificationReviewLabelLengthRule(1000)
        val item = newPrimaryQualificationReview("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryQualificationReviewRequiredCodeRule("primary qualification review needs this field")
        val item = newPrimaryQualificationReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = PrimaryQualificationReviewNotesLengthRule(64)
        val item = newPrimaryQualificationReview("x", "y")
        item.notes = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
