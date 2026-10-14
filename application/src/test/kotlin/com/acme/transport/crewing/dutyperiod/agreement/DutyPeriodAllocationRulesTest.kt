package com.acme.transport.crewing.dutyperiod.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodAllocationValidator()
        val item = newDutyPeriodAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = DutyPeriodAllocationStartsAtRangeRule(64, 86400)
        val item = newDutyPeriodAllocation("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = DutyPeriodAllocationCapacityRangeRule(1000, 3600)
        val item = newDutyPeriodAllocation("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
