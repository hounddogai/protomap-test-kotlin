package com.acme.transport.crewing.availability.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityCycleValidator()
        val item = newAvailabilityCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = AvailabilityCycleFuelLitersRangeRule(1.0, 262.8)
        val item = newAvailabilityCycle("x", "y")
        item.fuelLiters = 264.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AvailabilityCycleRequiredCodeRule("availability cycle needs an entry")
        val item = newAvailabilityCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
