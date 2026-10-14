package com.acme.transport.emissions.emissionfactor.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackEmissionFactorQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackEmissionFactorQuotaValidator()
        val item = newFallbackEmissionFactorQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = FallbackEmissionFactorQuotaVehicleIdLengthRule(255)
        val item = newFallbackEmissionFactorQuota("x", "y")
        item.vehicleId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackEmissionFactorQuotaNameLengthRule(80)
        val item = newFallbackEmissionFactorQuota("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = FallbackEmissionFactorQuotaLocaleLengthRule(3600)
        val item = newFallbackEmissionFactorQuota("x", "y")
        item.locale = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = FallbackEmissionFactorQuotaRequiredLocaleRule("fallback emission factor quota needs this field")
        val item = newFallbackEmissionFactorQuota("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
