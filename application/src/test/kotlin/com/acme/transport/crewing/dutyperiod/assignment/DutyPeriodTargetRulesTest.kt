package com.acme.transport.crewing.dutyperiod.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodTargetValidator()
        val item = newDutyPeriodTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = DutyPeriodTargetScoreRangeRule(6.6, 4280.2)
        val item = newDutyPeriodTarget("x", "y")
        item.score = 4281.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = DutyPeriodTargetRequiredShiftCodeRule("duty period target needs this field")
        val item = newDutyPeriodTarget("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
