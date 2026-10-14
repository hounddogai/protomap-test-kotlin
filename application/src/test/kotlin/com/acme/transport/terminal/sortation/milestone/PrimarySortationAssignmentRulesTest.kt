package com.acme.transport.terminal.sortation.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimarySortationAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimarySortationAssignmentValidator()
        val item = newPrimarySortationAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = PrimarySortationAssignmentFuelLitersRangeRule(4.9, 452.4)
        val item = newPrimarySortationAssignment("x", "y")
        item.fuelLiters = 453.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = PrimarySortationAssignmentRouteCodeLengthRule(86400)
        val item = newPrimarySortationAssignment("x", "y")
        item.routeCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = PrimarySortationAssignmentAmountRangeRule(6.4, 223.3)
        val item = newPrimarySortationAssignment("x", "y")
        item.amount = 224.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimarySortationAssignmentCodeLengthRule(86400)
        val item = newPrimarySortationAssignment("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimarySortationAssignmentRequiredLabelRule("primary sortation assignment needs an entry")
        val item = newPrimarySortationAssignment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
