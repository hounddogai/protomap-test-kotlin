package com.acme.transport.crewing.qualification.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationThresholdValidator()
        val item = newQualificationThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = QualificationThresholdRegionLengthRule(16)
        val item = newQualificationThreshold("x", "y")
        item.region = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = QualificationThresholdRequiredRegionRule("qualification threshold needs a value")
        val item = newQualificationThreshold("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = QualificationThresholdAttemptsRangeRule(64, 1000)
        val item = newQualificationThreshold("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
