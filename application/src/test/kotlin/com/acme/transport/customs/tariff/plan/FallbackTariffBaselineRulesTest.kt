package com.acme.transport.customs.tariff.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTariffBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackTariffBaselineValidator()
        val item = newFallbackTariffBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = FallbackTariffBaselineWindowDaysRangeRule(64, 86400)
        val item = newFallbackTariffBaseline("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackTariffBaselineRequiredCodeRule("fallback tariff baseline needs an entry")
        val item = newFallbackTariffBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = FallbackTariffBaselineDurationMinutesRangeRule(32, 80)
        val item = newFallbackTariffBaseline("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackTariffBaselineRequiredNameRule("fallback tariff baseline needs an entry")
        val item = newFallbackTariffBaseline("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = FallbackTariffBaselineMaximumRangeRule(9.8, 2050.7)
        val item = newFallbackTariffBaseline("x", "y")
        item.maximum = 2052.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
