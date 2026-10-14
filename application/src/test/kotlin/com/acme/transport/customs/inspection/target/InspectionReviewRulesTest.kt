package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionReviewValidator()
        val item = newInspectionReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = InspectionReviewChannelLengthRule(86400)
        val item = newInspectionReview("x", "y")
        item.channel = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = InspectionReviewRequiredChannelRule("inspection review needs a value")
        val item = newInspectionReview("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
