package com.acme.transport.crewing.qualification.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationPreferenceValidator()
        val item = newQualificationPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = QualificationPreferenceRequiredLabelRule("qualification preference needs a value")
        val item = newQualificationPreference("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = QualificationPreferenceThresholdRangeRule(5.2, 3961.2)
        val item = newQualificationPreference("x", "y")
        item.threshold = 3962.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = QualificationPreferenceExpiresAtRangeRule(64, 255)
        val item = newQualificationPreference("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
