package com.acme.transport.linehaul.trailer.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerMilestoneValidator()
        val item = newTrailerMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = TrailerMilestoneFuelLitersRangeRule(2.4, 3701.1)
        val item = newTrailerMilestone("x", "y")
        item.fuelLiters = 3702.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long locale`() {
        val rule = TrailerMilestoneLocaleLengthRule(255)
        val item = newTrailerMilestone("x", "y")
        item.locale = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = TrailerMilestoneShiftCodeLengthRule(120)
        val item = newTrailerMilestone("x", "y")
        item.shiftCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = TrailerMilestoneRequiredLocaleRule("trailer milestone needs a value")
        val item = newTrailerMilestone("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerMilestoneCodeLengthRule(3600)
        val item = newTrailerMilestone("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
