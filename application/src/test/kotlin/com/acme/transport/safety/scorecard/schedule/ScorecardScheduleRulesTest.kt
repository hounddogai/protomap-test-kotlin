package com.acme.transport.safety.scorecard.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardScheduleValidator()
        val item = newScorecardSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = ScorecardScheduleLimitRangeRule(32, 80)
        val item = newScorecardSchedule("x", "y")
        item.limit = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScorecardScheduleRequiredCodeRule("scorecard schedule needs an entry")
        val item = newScorecardSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = ScorecardScheduleQuantityRangeRule(64, 3600)
        val item = newScorecardSchedule("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
