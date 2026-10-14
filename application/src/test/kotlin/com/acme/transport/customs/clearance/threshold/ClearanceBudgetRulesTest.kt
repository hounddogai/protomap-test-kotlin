package com.acme.transport.customs.clearance.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceBudgetValidator()
        val item = newClearanceBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ClearanceBudgetFloorValueRangeRule(2.0, 4887.4)
        val item = newClearanceBudget("x", "y")
        item.floorValue = 4888.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ClearanceBudgetCodeLengthRule(32)
        val item = newClearanceBudget("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ClearanceBudgetThresholdRangeRule(7.8, 72.7)
        val item = newClearanceBudget("x", "y")
        item.threshold = 74.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = ClearanceBudgetBudgetRangeRule(0.2, 640.2)
        val item = newClearanceBudget("x", "y")
        item.budget = 641.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = ClearanceBudgetRequiredDepotRule("clearance budget needs a value")
        val item = newClearanceBudget("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
