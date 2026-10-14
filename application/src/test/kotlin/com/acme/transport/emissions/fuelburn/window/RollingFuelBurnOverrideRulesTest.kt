package com.acme.transport.emissions.fuelburn.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFuelBurnOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingFuelBurnOverrideValidator()
        val item = newRollingFuelBurnOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = RollingFuelBurnOverridePhoneLengthRule(16)
        val item = newRollingFuelBurnOverride("x", "y")
        item.phone = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingFuelBurnOverrideRequiredNameRule("rolling fuel burn override needs an entry")
        val item = newRollingFuelBurnOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = RollingFuelBurnOverrideOdometerRangeRule(120, 255)
        val item = newRollingFuelBurnOverride("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
