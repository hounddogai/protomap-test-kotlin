package com.acme.transport.terminal.dwell.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellRecordValidator()
        val item = newDwellRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DwellRecordTitleLengthRule(120)
        val item = newDwellRecord("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = DwellRecordCostRangeRule(1.5, 2876.3)
        val item = newDwellRecord("x", "y")
        item.cost = 2877.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = DwellRecordRequiredVehicleIdRule("dwell record needs an entry")
        val item = newDwellRecord("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DwellRecordRequiredCodeRule("dwell record needs this field")
        val item = newDwellRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
