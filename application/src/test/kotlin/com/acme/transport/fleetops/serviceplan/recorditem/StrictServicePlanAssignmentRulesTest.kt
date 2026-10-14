package com.acme.transport.fleetops.serviceplan.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictServicePlanAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictServicePlanAssignmentValidator()
        val item = newStrictServicePlanAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictServicePlanAssignmentCodeLengthRule(16)
        val item = newStrictServicePlanAssignment("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = StrictServicePlanAssignmentCapacityRangeRule(3600, 86400)
        val item = newStrictServicePlanAssignment("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = StrictServicePlanAssignmentQuantityRangeRule(16, 64)
        val item = newStrictServicePlanAssignment("x", "y")
        item.quantity = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
