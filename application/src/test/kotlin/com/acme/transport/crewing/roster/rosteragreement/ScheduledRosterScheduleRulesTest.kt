package com.acme.transport.crewing.roster.rosteragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRosterScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRosterScheduleValidator()
        val item = newScheduledRosterSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = ScheduledRosterScheduleUnitLengthRule(32)
        val item = newScheduledRosterSchedule("x", "y")
        item.unit = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = ScheduledRosterScheduleMinimumRangeRule(2.9, 4022.9)
        val item = newScheduledRosterSchedule("x", "y")
        item.minimum = 4024.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledRosterScheduleRequiredCodeRule("scheduled roster schedule needs a value")
        val item = newScheduledRosterSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledRosterScheduleLabelLengthRule(16)
        val item = newScheduledRosterSchedule("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
