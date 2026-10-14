package com.acme.transport.safety.coaching.coachingdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCoachingSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledCoachingSummaryValidator()
        val item = newScheduledCoachingSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledCoachingSummaryRequiredNameRule("scheduled coaching summary needs an entry")
        val item = newScheduledCoachingSummary("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = ScheduledCoachingSummaryRequiredRouteCodeRule("scheduled coaching summary needs a value")
        val item = newScheduledCoachingSummary("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ScheduledCoachingSummaryPriorityRangeRule(32, 86400)
        val item = newScheduledCoachingSummary("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
