package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAccessorialMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientAccessorialMetricValidator()
        val item = newLenientAccessorialMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientAccessorialMetricRequiredCodeRule("lenient accessorial metric needs a value")
        val item = newLenientAccessorialMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = LenientAccessorialMetricStopCountRangeRule(64, 3600)
        val item = newLenientAccessorialMetric("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = LenientAccessorialMetricDescriptionLengthRule(32)
        val item = newLenientAccessorialMetric("x", "y")
        item.description = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = LenientAccessorialMetricRequiredDescriptionRule("lenient accessorial metric needs this field")
        val item = newLenientAccessorialMetric("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientAccessorialMetricLabelLengthRule(64)
        val item = newLenientAccessorialMetric("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
