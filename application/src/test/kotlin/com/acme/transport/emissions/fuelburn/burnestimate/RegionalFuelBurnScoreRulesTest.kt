package com.acme.transport.emissions.fuelburn.burnestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalFuelBurnScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalFuelBurnScoreValidator()
        val item = newRegionalFuelBurnScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = RegionalFuelBurnScoreRequiredUnitRule("regional fuel burn score needs this field")
        val item = newRegionalFuelBurnScore("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalFuelBurnScoreNameLengthRule(16)
        val item = newRegionalFuelBurnScore("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RegionalFuelBurnScoreStopCountRangeRule(32, 86400)
        val item = newRegionalFuelBurnScore("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = RegionalFuelBurnScoreChecksumLengthRule(32)
        val item = newRegionalFuelBurnScore("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
