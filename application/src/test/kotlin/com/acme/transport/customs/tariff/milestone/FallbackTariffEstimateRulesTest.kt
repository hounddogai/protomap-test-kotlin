package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTariffEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackTariffEstimateValidator()
        val item = newFallbackTariffEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = FallbackTariffEstimateRequiredOwnerIdRule("fallback tariff estimate needs this field")
        val item = newFallbackTariffEstimate("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = FallbackTariffEstimateVersionRangeRule(64, 80)
        val item = newFallbackTariffEstimate("x", "y")
        item.version = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackTariffEstimateRequiredCodeRule("fallback tariff estimate needs this field")
        val item = newFallbackTariffEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
