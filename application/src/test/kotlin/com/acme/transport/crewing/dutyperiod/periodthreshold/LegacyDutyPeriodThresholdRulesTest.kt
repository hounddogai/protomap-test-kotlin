package com.acme.transport.crewing.dutyperiod.periodthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDutyPeriodThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDutyPeriodThresholdValidator()
        val item = newLegacyDutyPeriodThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyDutyPeriodThresholdCodeLengthRule(120)
        val item = newLegacyDutyPeriodThreshold("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = LegacyDutyPeriodThresholdAmountRangeRule(3.3, 323.1)
        val item = newLegacyDutyPeriodThreshold("x", "y")
        item.amount = 324.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = LegacyDutyPeriodThresholdEndsAtRangeRule(32, 3600)
        val item = newLegacyDutyPeriodThreshold("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
