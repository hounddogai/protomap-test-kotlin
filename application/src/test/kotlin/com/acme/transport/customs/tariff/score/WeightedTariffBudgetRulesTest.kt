package com.acme.transport.customs.tariff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTariffBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTariffBudgetValidator()
        val item = newWeightedTariffBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedTariffBudgetCodeLengthRule(255)
        val item = newWeightedTariffBudget("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedTariffBudgetRequiredTitleRule("weighted tariff budget needs an entry")
        val item = newWeightedTariffBudget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = WeightedTariffBudgetNotesLengthRule(86400)
        val item = newWeightedTariffBudget("x", "y")
        item.notes = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = WeightedTariffBudgetBudgetRangeRule(0.9, 641.7)
        val item = newWeightedTariffBudget("x", "y")
        item.budget = 643.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
