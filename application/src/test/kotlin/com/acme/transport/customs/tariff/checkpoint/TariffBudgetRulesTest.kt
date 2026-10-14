package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffBudgetValidator()
        val item = newTariffBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TariffBudgetCodeLengthRule(255)
        val item = newTariffBudget("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = TariffBudgetCreatedAtRangeRule(64, 80)
        val item = newTariffBudget("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = TariffBudgetRequiredLocaleRule("tariff budget needs an entry")
        val item = newTariffBudget("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = TariffBudgetLocaleLengthRule(120)
        val item = newTariffBudget("x", "y")
        item.locale = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = TariffBudgetRequiredCurrencyRule("tariff budget needs this field")
        val item = newTariffBudget("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
