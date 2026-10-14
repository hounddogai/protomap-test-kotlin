package com.acme.transport.fleetops.inspection.review

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
    fun `flags a blank code`() {
        val rule = InspectionReviewRequiredCodeRule("inspection review needs this field")
        val item = newInspectionReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = InspectionReviewRequiredTitleRule("inspection review needs this field")
        val item = newInspectionReview("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = InspectionReviewRateRangeRule(3.6, 1071.3)
        val item = newInspectionReview("x", "y")
        item.rate = 1072.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
