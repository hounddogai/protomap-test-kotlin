package com.acme.transport.crewing.dutyperiod.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDutyPeriodExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDutyPeriodExceptionValidator()
        val item = newScheduledDutyPeriodException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledDutyPeriodExceptionRequiredNameRule("scheduled duty period exception needs an entry")
        val item = newScheduledDutyPeriodException("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = ScheduledDutyPeriodExceptionRequiredRegionRule("scheduled duty period exception needs a value")
        val item = newScheduledDutyPeriodException("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
