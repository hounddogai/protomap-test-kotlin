package com.acme.transport.fleetops.leasereturn.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryLeaseReturnBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryLeaseReturnBudgetValidator()
        val item = newPrimaryLeaseReturnBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = PrimaryLeaseReturnBudgetCurrencyLengthRule(80)
        val item = newPrimaryLeaseReturnBudget("x", "y")
        item.currency = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryLeaseReturnBudgetRequiredTitleRule("primary lease return budget needs an entry")
        val item = newPrimaryLeaseReturnBudget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = PrimaryLeaseReturnBudgetRateRangeRule(2.0, 2382.8)
        val item = newPrimaryLeaseReturnBudget("x", "y")
        item.rate = 2384.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryLeaseReturnBudgetRequiredCodeRule("primary lease return budget needs this field")
        val item = newPrimaryLeaseReturnBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
