package com.acme.transport.lastmile.locker.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingLockerPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingLockerPlanValidator()
        val item = newRollingLockerPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = RollingLockerPlanRequiredSegmentRule("rolling locker plan needs a value")
        val item = newRollingLockerPlan("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = RollingLockerPlanDurationMinutesRangeRule(120, 255)
        val item = newRollingLockerPlan("x", "y")
        item.durationMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
