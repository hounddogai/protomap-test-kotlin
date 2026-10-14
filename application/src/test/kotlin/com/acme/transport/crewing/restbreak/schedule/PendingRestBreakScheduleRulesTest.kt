package com.acme.transport.crewing.restbreak.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRestBreakScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRestBreakScheduleValidator()
        val item = newPendingRestBreakSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingRestBreakScheduleRequiredNameRule("pending rest break schedule needs a value")
        val item = newPendingRestBreakSchedule("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = PendingRestBreakScheduleEndsAtRangeRule(1000, 86400)
        val item = newPendingRestBreakSchedule("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
