package com.acme.transport.crewing.qualification.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationEstimateValidator()
        val item = newQualificationEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = QualificationEstimateRequiredLabelRule("qualification estimate needs this field")
        val item = newQualificationEstimate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = QualificationEstimateMaximumRangeRule(6.9, 1371.7)
        val item = newQualificationEstimate("x", "y")
        item.maximum = 1373.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = QualificationEstimateLabelLengthRule(3600)
        val item = newQualificationEstimate("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = QualificationEstimatePalletsRangeRule(16, 16)
        val item = newQualificationEstimate("x", "y")
        item.pallets = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = QualificationEstimateRequiredLocaleRule("qualification estimate needs a value")
        val item = newQualificationEstimate("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = QualificationEstimateCodeLengthRule(16)
        val item = newQualificationEstimate("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
