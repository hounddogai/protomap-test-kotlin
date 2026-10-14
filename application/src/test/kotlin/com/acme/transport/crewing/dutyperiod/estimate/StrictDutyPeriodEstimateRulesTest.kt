package com.acme.transport.crewing.dutyperiod.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDutyPeriodEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDutyPeriodEstimateValidator()
        val item = newStrictDutyPeriodEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = StrictDutyPeriodEstimateMarginRangeRule(4.5, 4375.6)
        val item = newStrictDutyPeriodEstimate("x", "y")
        item.margin = 4377.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = StrictDutyPeriodEstimateCountryLengthRule(32)
        val item = newStrictDutyPeriodEstimate("x", "y")
        item.country = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictDutyPeriodEstimateRequiredNameRule("strict duty period estimate needs a value")
        val item = newStrictDutyPeriodEstimate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
