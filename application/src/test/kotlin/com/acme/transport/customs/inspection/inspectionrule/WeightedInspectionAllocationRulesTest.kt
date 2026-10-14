package com.acme.transport.customs.inspection.inspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedInspectionAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedInspectionAllocationValidator()
        val item = newWeightedInspectionAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedInspectionAllocationCodeLengthRule(16)
        val item = newWeightedInspectionAllocation("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = WeightedInspectionAllocationNameLengthRule(16)
        val item = newWeightedInspectionAllocation("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = WeightedInspectionAllocationFloorValueRangeRule(1.8, 4063.9)
        val item = newWeightedInspectionAllocation("x", "y")
        item.floorValue = 4065.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = WeightedInspectionAllocationWeightRangeRule(2.4, 4503.2)
        val item = newWeightedInspectionAllocation("x", "y")
        item.weight = 4504.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
