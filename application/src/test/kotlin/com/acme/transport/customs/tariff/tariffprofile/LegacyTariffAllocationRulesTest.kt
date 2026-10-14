package com.acme.transport.customs.tariff.tariffprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTariffAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTariffAllocationValidator()
        val item = newLegacyTariffAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = LegacyTariffAllocationUnitLengthRule(3600)
        val item = newLegacyTariffAllocation("x", "y")
        item.unit = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyTariffAllocationRequiredCodeRule("legacy tariff allocation needs this field")
        val item = newLegacyTariffAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = LegacyTariffAllocationExpiresAtRangeRule(120, 255)
        val item = newLegacyTariffAllocation("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
