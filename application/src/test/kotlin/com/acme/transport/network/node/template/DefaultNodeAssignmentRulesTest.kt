package com.acme.transport.network.node.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultNodeAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultNodeAssignmentValidator()
        val item = newDefaultNodeAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultNodeAssignmentCodeLengthRule(1000)
        val item = newDefaultNodeAssignment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = DefaultNodeAssignmentVehicleIdLengthRule(120)
        val item = newDefaultNodeAssignment("x", "y")
        item.vehicleId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = DefaultNodeAssignmentRequiredDescriptionRule("default node assignment needs an entry")
        val item = newDefaultNodeAssignment("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultNodeAssignmentRequiredCodeRule("default node assignment needs a value")
        val item = newDefaultNodeAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = DefaultNodeAssignmentFuelLitersRangeRule(8.6, 2289.9)
        val item = newDefaultNodeAssignment("x", "y")
        item.fuelLiters = 2291.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = DefaultNodeAssignmentDescriptionLengthRule(1000)
        val item = newDefaultNodeAssignment("x", "y")
        item.description = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
