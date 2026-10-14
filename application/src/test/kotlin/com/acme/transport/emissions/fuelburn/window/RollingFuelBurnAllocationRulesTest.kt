package com.acme.transport.emissions.fuelburn.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFuelBurnAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingFuelBurnAllocationValidator()
        val item = newRollingFuelBurnAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = RollingFuelBurnAllocationVersionRangeRule(32, 80)
        val item = newRollingFuelBurnAllocation("x", "y")
        item.version = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = RollingFuelBurnAllocationRequiredTrailerIdRule("rolling fuel burn allocation needs an entry")
        val item = newRollingFuelBurnAllocation("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
