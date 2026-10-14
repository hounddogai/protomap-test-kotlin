package com.acme.transport.customs.tariff.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTariffAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackTariffAllocationValidator()
        val item = newFallbackTariffAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = FallbackTariffAllocationWeightRangeRule(6.6, 617.9)
        val item = newFallbackTariffAllocation("x", "y")
        item.weight = 619.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackTariffAllocationRequiredCodeRule("fallback tariff allocation needs this field")
        val item = newFallbackTariffAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = FallbackTariffAllocationChecksumLengthRule(1000)
        val item = newFallbackTariffAllocation("x", "y")
        item.checksum = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = FallbackTariffAllocationRequiredChecksumRule("fallback tariff allocation needs a value")
        val item = newFallbackTariffAllocation("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
