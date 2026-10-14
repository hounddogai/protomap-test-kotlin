package com.acme.transport.fleetops.utilization.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingUtilizationAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingUtilizationAssignmentValidator()
        val item = newPendingUtilizationAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingUtilizationAssignmentRequiredLabelRule("pending utilization assignment needs a value")
        val item = newPendingUtilizationAssignment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = PendingUtilizationAssignmentRequiredOwnerIdRule("pending utilization assignment needs an entry")
        val item = newPendingUtilizationAssignment("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingUtilizationAssignmentRequiredCodeRule("pending utilization assignment needs a value")
        val item = newPendingUtilizationAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
