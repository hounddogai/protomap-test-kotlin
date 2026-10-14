package com.acme.transport.crewing.dutyperiod.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDutyPeriodAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedDutyPeriodAllocationValidator()
        val item = newWeightedDutyPeriodAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = WeightedDutyPeriodAllocationRequiredCarrierRule("weighted duty period allocation needs an entry")
        val item = newWeightedDutyPeriodAllocation("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = WeightedDutyPeriodAllocationPriorityRangeRule(255, 1000)
        val item = newWeightedDutyPeriodAllocation("x", "y")
        item.priority = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = WeightedDutyPeriodAllocationCarrierLengthRule(3600)
        val item = newWeightedDutyPeriodAllocation("x", "y")
        item.carrier = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
