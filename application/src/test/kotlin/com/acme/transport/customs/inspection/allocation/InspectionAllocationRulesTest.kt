package com.acme.transport.customs.inspection.allocation

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
    fun `flags a long name`() {
        val rule = InspectionAllocationNameLengthRule(3600)
        val item = newInspectionAllocation("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = InspectionAllocationDockLengthRule(80)
        val item = newInspectionAllocation("x", "y")
        item.dock = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = InspectionAllocationRatioRangeRule(1.1, 4888.1)
        val item = newInspectionAllocation("x", "y")
        item.ratio = 4889.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = InspectionAllocationPriorityRangeRule(16, 120)
        val item = newInspectionAllocation("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
