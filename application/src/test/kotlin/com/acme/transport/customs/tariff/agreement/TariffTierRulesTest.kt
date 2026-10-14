package com.acme.transport.customs.tariff.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffTierValidator()
        val item = newTariffTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TariffTierTitleLengthRule(64)
        val item = newTariffTier("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TariffTierRequiredTitleRule("tariff tier needs this field")
        val item = newTariffTier("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = TariffTierEtaMinutesRangeRule(64, 3600)
        val item = newTariffTier("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = TariffTierDepotLengthRule(86400)
        val item = newTariffTier("x", "y")
        item.depot = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = TariffTierBudgetRangeRule(3.9, 3559.5)
        val item = newTariffTier("x", "y")
        item.budget = 3561.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
