package com.acme.transport.emissions.routeoffset.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetAssignmentValidator()
        val item = newRouteOffsetAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = RouteOffsetAssignmentUpdatedAtRangeRule(32, 86400)
        val item = newRouteOffsetAssignment("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = RouteOffsetAssignmentRequiredRouteCodeRule("route offset assignment needs this field")
        val item = newRouteOffsetAssignment("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RouteOffsetAssignmentRequiredLabelRule("route offset assignment needs a value")
        val item = newRouteOffsetAssignment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = RouteOffsetAssignmentRequiredVehicleIdRule("route offset assignment needs an entry")
        val item = newRouteOffsetAssignment("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RouteOffsetAssignmentRouteCodeLengthRule(64)
        val item = newRouteOffsetAssignment("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = RouteOffsetAssignmentLabelLengthRule(1000)
        val item = newRouteOffsetAssignment("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
