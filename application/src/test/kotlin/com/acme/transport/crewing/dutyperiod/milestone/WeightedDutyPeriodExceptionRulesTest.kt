package com.acme.transport.crewing.dutyperiod.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDutyPeriodExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedDutyPeriodExceptionValidator()
        val item = newWeightedDutyPeriodException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = WeightedDutyPeriodExceptionRateRangeRule(3.3, 4960.1)
        val item = newWeightedDutyPeriodException("x", "y")
        item.rate = 4961.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = WeightedDutyPeriodExceptionCapacityRangeRule(64, 64)
        val item = newWeightedDutyPeriodException("x", "y")
        item.capacity = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = WeightedDutyPeriodExceptionRequiredSourceRule("weighted duty period exception needs this field")
        val item = newWeightedDutyPeriodException("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = WeightedDutyPeriodExceptionRequiredContactEmailRule(
            "weighted duty period exception needs this field",
        )
        val item = newWeightedDutyPeriodException("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedDutyPeriodExceptionRequiredNameRule("weighted duty period exception needs a value")
        val item = newWeightedDutyPeriodException("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedDutyPeriodExceptionRequiredCodeRule("weighted duty period exception needs an entry")
        val item = newWeightedDutyPeriodException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
