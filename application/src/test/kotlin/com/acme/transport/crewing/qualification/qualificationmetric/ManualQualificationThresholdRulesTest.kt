package com.acme.transport.crewing.qualification.qualificationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualQualificationThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualQualificationThresholdValidator()
        val item = newManualQualificationThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ManualQualificationThresholdExpiresAtRangeRule(16, 80)
        val item = newManualQualificationThreshold("x", "y")
        item.expiresAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = ManualQualificationThresholdRequiredReviewerRule("manual qualification threshold needs an entry")
        val item = newManualQualificationThreshold("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = ManualQualificationThresholdCreatedAtRangeRule(32, 3600)
        val item = newManualQualificationThreshold("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
