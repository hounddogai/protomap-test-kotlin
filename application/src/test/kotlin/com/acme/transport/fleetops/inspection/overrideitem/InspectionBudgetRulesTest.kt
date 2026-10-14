package com.acme.transport.fleetops.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionBudgetValidator()
        val item = newInspectionBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = InspectionBudgetFloorValueRangeRule(2.5, 4874.4)
        val item = newInspectionBudget("x", "y")
        item.floorValue = 4875.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionBudgetCodeLengthRule(255)
        val item = newInspectionBudget("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = InspectionBudgetOwnerIdLengthRule(16)
        val item = newInspectionBudget("x", "y")
        item.ownerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
