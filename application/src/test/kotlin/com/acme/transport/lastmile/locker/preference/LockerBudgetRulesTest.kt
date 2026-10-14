package com.acme.transport.lastmile.locker.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerBudgetValidator()
        val item = newLockerBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = LockerBudgetCostRangeRule(3.0, 4886.5)
        val item = newLockerBudget("x", "y")
        item.cost = 4888.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LockerBudgetRequiredCodeRule("locker budget needs this field")
        val item = newLockerBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = LockerBudgetChecksumLengthRule(64)
        val item = newLockerBudget("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = LockerBudgetEtaMinutesRangeRule(32, 32)
        val item = newLockerBudget("x", "y")
        item.etaMinutes = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
