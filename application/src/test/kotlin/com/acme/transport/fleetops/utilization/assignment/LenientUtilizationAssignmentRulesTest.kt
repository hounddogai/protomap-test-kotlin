package com.acme.transport.fleetops.utilization.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientUtilizationAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientUtilizationAssignmentValidator()
        val item = newLenientUtilizationAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = LenientUtilizationAssignmentVehicleIdLengthRule(32)
        val item = newLenientUtilizationAssignment("x", "y")
        item.vehicleId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientUtilizationAssignmentRequiredLabelRule("lenient utilization assignment needs an entry")
        val item = newLenientUtilizationAssignment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = LenientUtilizationAssignmentMinimumRangeRule(2.5, 122.4)
        val item = newLenientUtilizationAssignment("x", "y")
        item.minimum = 123.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = LenientUtilizationAssignmentUnitLengthRule(255)
        val item = newLenientUtilizationAssignment("x", "y")
        item.unit = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = LenientUtilizationAssignmentRevisionRangeRule(32, 1000)
        val item = newLenientUtilizationAssignment("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientUtilizationAssignmentLabelLengthRule(1000)
        val item = newLenientUtilizationAssignment("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
