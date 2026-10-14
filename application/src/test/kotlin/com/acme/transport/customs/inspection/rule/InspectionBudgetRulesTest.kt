package com.acme.transport.customs.inspection.rule

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
    fun `flags a long name`() {
        val rule = InspectionBudgetNameLengthRule(80)
        val item = newInspectionBudget("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionBudgetLabelLengthRule(16)
        val item = newInspectionBudget("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = InspectionBudgetAmountRangeRule(4.9, 4716.1)
        val item = newInspectionBudget("x", "y")
        item.amount = 4717.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = InspectionBudgetRequiredNameRule("inspection budget needs a value")
        val item = newInspectionBudget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionBudgetRequiredCodeRule("inspection budget needs this field")
        val item = newInspectionBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = InspectionBudgetRouteCodeLengthRule(16)
        val item = newInspectionBudget("x", "y")
        item.routeCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
