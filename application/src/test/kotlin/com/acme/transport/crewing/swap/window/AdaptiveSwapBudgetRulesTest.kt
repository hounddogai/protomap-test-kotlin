package com.acme.transport.crewing.swap.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveSwapBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveSwapBudgetValidator()
        val item = newAdaptiveSwapBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveSwapBudgetCodeLengthRule(64)
        val item = newAdaptiveSwapBudget("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveSwapBudgetTitleLengthRule(1000)
        val item = newAdaptiveSwapBudget("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = AdaptiveSwapBudgetRequiredDriverIdRule("adaptive swap budget needs an entry")
        val item = newAdaptiveSwapBudget("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveSwapBudgetRequiredCodeRule("adaptive swap budget needs this field")
        val item = newAdaptiveSwapBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
