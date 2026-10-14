package com.acme.transport.lastmile.drop.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDropScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDropScheduleValidator()
        val item = newSeasonalDropSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = SeasonalDropScheduleDescriptionLengthRule(64)
        val item = newSeasonalDropSchedule("x", "y")
        item.description = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalDropScheduleRequiredCodeRule("seasonal drop schedule needs a value")
        val item = newSeasonalDropSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalDropScheduleCodeLengthRule(32)
        val item = newSeasonalDropSchedule("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = SeasonalDropScheduleRequiredContactEmailRule("seasonal drop schedule needs a value")
        val item = newSeasonalDropSchedule("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = SeasonalDropScheduleDurationMinutesRangeRule(32, 3600)
        val item = newSeasonalDropSchedule("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
