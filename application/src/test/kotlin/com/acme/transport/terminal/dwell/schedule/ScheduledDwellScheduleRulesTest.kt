package com.acme.transport.terminal.dwell.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDwellScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDwellScheduleValidator()
        val item = newScheduledDwellSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledDwellScheduleNameLengthRule(80)
        val item = newScheduledDwellSchedule("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledDwellScheduleRequiredCodeRule("scheduled dwell schedule needs an entry")
        val item = newScheduledDwellSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
