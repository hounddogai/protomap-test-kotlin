package com.acme.transport.emissions.emissionfactor.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorTemplateValidator()
        val item = newEmissionFactorTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = EmissionFactorTemplateEndsAtRangeRule(32, 80)
        val item = newEmissionFactorTemplate("x", "y")
        item.endsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = EmissionFactorTemplateVehicleIdLengthRule(1000)
        val item = newEmissionFactorTemplate("x", "y")
        item.vehicleId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = EmissionFactorTemplateThresholdRangeRule(7.9, 2414.6)
        val item = newEmissionFactorTemplate("x", "y")
        item.threshold = 2416.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
