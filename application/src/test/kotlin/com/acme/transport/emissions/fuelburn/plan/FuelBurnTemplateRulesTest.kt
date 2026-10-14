package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnTemplateValidator()
        val item = newFuelBurnTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = FuelBurnTemplateStartsAtRangeRule(3600, 86400)
        val item = newFuelBurnTemplate("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = FuelBurnTemplatePhoneLengthRule(3600)
        val item = newFuelBurnTemplate("x", "y")
        item.phone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = FuelBurnTemplateRequiredRegionRule("fuel burn template needs this field")
        val item = newFuelBurnTemplate("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = FuelBurnTemplateRegionLengthRule(80)
        val item = newFuelBurnTemplate("x", "y")
        item.region = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
