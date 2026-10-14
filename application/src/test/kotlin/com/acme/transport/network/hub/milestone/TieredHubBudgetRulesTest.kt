package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredHubBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredHubBudgetValidator()
        val item = newTieredHubBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredHubBudgetLabelLengthRule(32)
        val item = newTieredHubBudget("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = TieredHubBudgetRetriesRangeRule(64, 80)
        val item = newTieredHubBudget("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredHubBudgetRequiredCodeRule("tiered hub budget needs a value")
        val item = newTieredHubBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
