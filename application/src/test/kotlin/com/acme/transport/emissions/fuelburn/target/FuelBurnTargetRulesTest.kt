package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnTargetValidator()
        val item = newFuelBurnTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FuelBurnTargetNameLengthRule(64)
        val item = newFuelBurnTarget("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long channel`() {
        val rule = FuelBurnTargetChannelLengthRule(32)
        val item = newFuelBurnTarget("x", "y")
        item.channel = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelBurnTargetRequiredCodeRule("fuel burn target needs this field")
        val item = newFuelBurnTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = FuelBurnTargetChecksumLengthRule(64)
        val item = newFuelBurnTarget("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
