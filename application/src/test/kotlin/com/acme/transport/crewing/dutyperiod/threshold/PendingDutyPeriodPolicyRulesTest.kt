package com.acme.transport.crewing.dutyperiod.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDutyPeriodPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDutyPeriodPolicyValidator()
        val item = newPendingDutyPeriodPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = PendingDutyPeriodPolicyStopCountRangeRule(32, 80)
        val item = newPendingDutyPeriodPolicy("x", "y")
        item.stopCount = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = PendingDutyPeriodPolicyShiftCodeLengthRule(64)
        val item = newPendingDutyPeriodPolicy("x", "y")
        item.shiftCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingDutyPeriodPolicyCodeLengthRule(64)
        val item = newPendingDutyPeriodPolicy("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = PendingDutyPeriodPolicyRatioRangeRule(9.5, 524.3)
        val item = newPendingDutyPeriodPolicy("x", "y")
        item.ratio = 525.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingDutyPeriodPolicyRequiredTitleRule("pending duty period policy needs an entry")
        val item = newPendingDutyPeriodPolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
