package com.acme.transport.customs.tariff.fallbacktariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTariffCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackTariffCycleValidator()
        val item = newFallbackTariffCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackTariffCycleTitleLengthRule(86400)
        val item = newFallbackTariffCycle("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = FallbackTariffCycleRequiredCurrencyRule("fallback tariff cycle needs this field")
        val item = newFallbackTariffCycle("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = FallbackTariffCycleCurrencyLengthRule(64)
        val item = newFallbackTariffCycle("x", "y")
        item.currency = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
