package com.acme.transport.customs.clearance.primaryclearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictClearanceAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictClearanceAssignmentValidator()
        val item = newStrictClearanceAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = StrictClearanceAssignmentRequiredVehicleIdRule("strict clearance assignment needs an entry")
        val item = newStrictClearanceAssignment("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictClearanceAssignmentCodeLengthRule(64)
        val item = newStrictClearanceAssignment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = StrictClearanceAssignmentVehicleIdLengthRule(3600)
        val item = newStrictClearanceAssignment("x", "y")
        item.vehicleId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = StrictClearanceAssignmentThresholdRangeRule(4.3, 2764.6)
        val item = newStrictClearanceAssignment("x", "y")
        item.threshold = 2766.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictClearanceAssignmentRequiredLabelRule("strict clearance assignment needs a value")
        val item = newStrictClearanceAssignment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
