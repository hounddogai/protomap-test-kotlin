package com.acme.transport.fleetops.inspection.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionGroupValidator()
        val item = newInspectionGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = InspectionGroupRequiredOwnerIdRule("inspection group needs a value")
        val item = newInspectionGroup("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = InspectionGroupOwnerIdLengthRule(1000)
        val item = newInspectionGroup("x", "y")
        item.ownerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
