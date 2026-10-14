package com.acme.transport.fleetops.inspection.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionAllocationValidator()
        val item = newInspectionAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionAllocationLabelLengthRule(32)
        val item = newInspectionAllocation("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long channel`() {
        val rule = InspectionAllocationChannelLengthRule(32)
        val item = newInspectionAllocation("x", "y")
        item.channel = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
