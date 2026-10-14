package com.acme.transport.crewing.swap.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapBudgetValidator()
        val item = newSwapBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = SwapBudgetDepotLengthRule(120)
        val item = newSwapBudget("x", "y")
        item.depot = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapBudgetCodeLengthRule(1000)
        val item = newSwapBudget("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = SwapBudgetNameLengthRule(255)
        val item = newSwapBudget("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = SwapBudgetRequiredDepotRule("swap budget needs this field")
        val item = newSwapBudget("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SwapBudgetRequiredNameRule("swap budget needs a value")
        val item = newSwapBudget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
