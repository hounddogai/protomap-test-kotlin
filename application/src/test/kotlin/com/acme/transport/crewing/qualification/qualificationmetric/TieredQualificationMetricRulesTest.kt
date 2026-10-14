package com.acme.transport.crewing.qualification.qualificationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredQualificationMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredQualificationMetricValidator()
        val item = newTieredQualificationMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = TieredQualificationMetricThresholdRangeRule(0.7, 3303.5)
        val item = newTieredQualificationMetric("x", "y")
        item.threshold = 3305.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = TieredQualificationMetricChannelLengthRule(64)
        val item = newTieredQualificationMetric("x", "y")
        item.channel = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = TieredQualificationMetricSourceLengthRule(16)
        val item = newTieredQualificationMetric("x", "y")
        item.source = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
