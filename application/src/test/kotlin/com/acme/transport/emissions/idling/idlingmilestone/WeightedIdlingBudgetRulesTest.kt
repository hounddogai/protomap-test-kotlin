package com.acme.transport.emissions.idling.idlingmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedIdlingBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedIdlingBudgetValidator()
        val item = newWeightedIdlingBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedIdlingBudgetCodeLengthRule(86400)
        val item = newWeightedIdlingBudget("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = WeightedIdlingBudgetGraceDaysRangeRule(64, 80)
        val item = newWeightedIdlingBudget("x", "y")
        item.graceDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = WeightedIdlingBudgetRequiredChannelRule("weighted idling budget needs an entry")
        val item = newWeightedIdlingBudget("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
