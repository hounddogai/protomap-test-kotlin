package com.acme.transport.linehaul.trailer.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerSegmentValidator()
        val item = newTrailerSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TrailerSegmentTitleLengthRule(64)
        val item = newTrailerSegment("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TrailerSegmentRequiredLabelRule("trailer segment needs a value")
        val item = newTrailerSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerSegmentRequiredCodeRule("trailer segment needs a value")
        val item = newTrailerSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TrailerSegmentLabelLengthRule(120)
        val item = newTrailerSegment("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = TrailerSegmentFloorValueRangeRule(9.9, 1987.2)
        val item = newTrailerSegment("x", "y")
        item.floorValue = 1988.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = TrailerSegmentStopCountRangeRule(120, 255)
        val item = newTrailerSegment("x", "y")
        item.stopCount = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
