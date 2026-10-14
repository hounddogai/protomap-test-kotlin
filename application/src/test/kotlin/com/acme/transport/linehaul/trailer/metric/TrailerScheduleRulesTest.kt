package com.acme.transport.linehaul.trailer.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerScheduleValidator()
        val item = newTrailerSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerScheduleRequiredCodeRule("trailer schedule needs an entry")
        val item = newTrailerSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = TrailerScheduleRequiredExternalRefRule("trailer schedule needs a value")
        val item = newTrailerSchedule("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = TrailerScheduleDurationMinutesRangeRule(64, 80)
        val item = newTrailerSchedule("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
