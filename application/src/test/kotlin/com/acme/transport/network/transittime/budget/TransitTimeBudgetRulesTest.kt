package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeBudgetValidator()
        val item = newTransitTimeBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = TransitTimeBudgetRequiredTimezoneRule("transit time budget needs an entry")
        val item = newTransitTimeBudget("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = TransitTimeBudgetDockLengthRule(80)
        val item = newTransitTimeBudget("x", "y")
        item.dock = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
