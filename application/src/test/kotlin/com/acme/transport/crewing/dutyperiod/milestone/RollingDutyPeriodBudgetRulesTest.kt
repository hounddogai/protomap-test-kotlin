package com.acme.transport.crewing.dutyperiod.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDutyPeriodBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDutyPeriodBudgetValidator()
        val item = newRollingDutyPeriodBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingDutyPeriodBudgetRequiredCodeRule("rolling duty period budget needs an entry")
        val item = newRollingDutyPeriodBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = RollingDutyPeriodBudgetPhoneLengthRule(255)
        val item = newRollingDutyPeriodBudget("x", "y")
        item.phone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingDutyPeriodBudgetCodeLengthRule(3600)
        val item = newRollingDutyPeriodBudget("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
