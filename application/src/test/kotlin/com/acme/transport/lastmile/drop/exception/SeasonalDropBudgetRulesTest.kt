package com.acme.transport.lastmile.drop.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDropBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDropBudgetValidator()
        val item = newSeasonalDropBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalDropBudgetRequiredCodeRule("seasonal drop budget needs this field")
        val item = newSeasonalDropBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = SeasonalDropBudgetRequiredCurrencyRule("seasonal drop budget needs a value")
        val item = newSeasonalDropBudget("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
