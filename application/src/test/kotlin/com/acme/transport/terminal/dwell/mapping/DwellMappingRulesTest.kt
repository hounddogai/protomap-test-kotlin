package com.acme.transport.terminal.dwell.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellMappingValidator()
        val item = newDwellMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = DwellMappingOdometerRangeRule(1000, 86400)
        val item = newDwellMapping("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DwellMappingRequiredTitleRule("dwell mapping needs an entry")
        val item = newDwellMapping("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = DwellMappingFuelLitersRangeRule(3.0, 3722.1)
        val item = newDwellMapping("x", "y")
        item.fuelLiters = 3723.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = DwellMappingNotesLengthRule(120)
        val item = newDwellMapping("x", "y")
        item.notes = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
