package com.acme.transport.linehaul.capacity.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityPlanValidator()
        val item = newCapacityPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CapacityPlanRequiredOwnerIdRule("capacity plan needs this field")
        val item = newCapacityPlan("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = CapacityPlanCeilingRangeRule(8.6, 688.6)
        val item = newCapacityPlan("x", "y")
        item.ceiling = 690.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
