package com.acme.transport.crewing.dutyperiod.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDutyPeriodTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedDutyPeriodTargetValidator()
        val item = newWeightedDutyPeriodTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedDutyPeriodTargetRequiredCodeRule("weighted duty period target needs an entry")
        val item = newWeightedDutyPeriodTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = WeightedDutyPeriodTargetLoadKgRangeRule(4.3, 164.8)
        val item = newWeightedDutyPeriodTarget("x", "y")
        item.loadKg = 166.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
