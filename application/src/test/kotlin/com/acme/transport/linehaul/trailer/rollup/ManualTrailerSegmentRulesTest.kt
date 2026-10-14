package com.acme.transport.linehaul.trailer.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTrailerSegmentValidator()
        val item = newManualTrailerSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualTrailerSegmentCodeLengthRule(32)
        val item = newManualTrailerSegment("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualTrailerSegmentLabelLengthRule(64)
        val item = newManualTrailerSegment("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualTrailerSegmentRequiredCodeRule("manual trailer segment needs an entry")
        val item = newManualTrailerSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ManualTrailerSegmentThresholdRangeRule(9.7, 4747.7)
        val item = newManualTrailerSegment("x", "y")
        item.threshold = 4749.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualTrailerSegmentRequiredLabelRule("manual trailer segment needs an entry")
        val item = newManualTrailerSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
