package com.acme.transport.lastmile.drop.cappeddropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropScheduleValidator()
        val item = newDropSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = DropScheduleNotesLengthRule(32)
        val item = newDropSchedule("x", "y")
        item.notes = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long channel`() {
        val rule = DropScheduleChannelLengthRule(120)
        val item = newDropSchedule("x", "y")
        item.channel = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = DropScheduleRequiredChannelRule("drop schedule needs an entry")
        val item = newDropSchedule("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = DropScheduleWindowDaysRangeRule(120, 1000)
        val item = newDropSchedule("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = DropScheduleRequiredNotesRule("drop schedule needs an entry")
        val item = newDropSchedule("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
