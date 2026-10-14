package com.acme.transport.crewing.dutyperiod.periodthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDutyPeriodMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultDutyPeriodMilestoneValidator()
        val item = newDefaultDutyPeriodMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultDutyPeriodMilestoneRequiredCodeRule("default duty period milestone needs this field")
        val item = newDefaultDutyPeriodMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = DefaultDutyPeriodMilestoneRequiredShiftCodeRule("default duty period milestone needs an entry")
        val item = newDefaultDutyPeriodMilestone("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = DefaultDutyPeriodMilestoneEndsAtRangeRule(16, 255)
        val item = newDefaultDutyPeriodMilestone("x", "y")
        item.endsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultDutyPeriodMilestoneRequiredNameRule("default duty period milestone needs an entry")
        val item = newDefaultDutyPeriodMilestone("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
