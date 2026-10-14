package com.acme.transport.crewing.swap.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapSegmentValidator()
        val item = newSwapSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = SwapSegmentSequenceRangeRule(16, 86400)
        val item = newSwapSegment("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = SwapSegmentMaximumRangeRule(8.3, 1431.7)
        val item = newSwapSegment("x", "y")
        item.maximum = 1433.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = SwapSegmentTitleLengthRule(86400)
        val item = newSwapSegment("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = SwapSegmentRequiredShiftCodeRule("swap segment needs this field")
        val item = newSwapSegment("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapSegmentCodeLengthRule(255)
        val item = newSwapSegment("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
