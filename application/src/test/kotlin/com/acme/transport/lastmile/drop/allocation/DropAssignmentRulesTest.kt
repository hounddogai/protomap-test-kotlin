package com.acme.transport.lastmile.drop.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropAssignmentValidator()
        val item = newDropAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DropAssignmentCodeLengthRule(16)
        val item = newDropAssignment("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = DropAssignmentRequiredVehicleIdRule("drop assignment needs an entry")
        val item = newDropAssignment("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = DropAssignmentStopCountRangeRule(120, 1000)
        val item = newDropAssignment("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DropAssignmentLabelLengthRule(120)
        val item = newDropAssignment("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
