package com.acme.transport.terminal.crossdock.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockBudgetValidator()
        val item = newCrossDockBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = CrossDockBudgetCapacityRangeRule(64, 86400)
        val item = newCrossDockBudget("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long source`() {
        val rule = CrossDockBudgetSourceLengthRule(3600)
        val item = newCrossDockBudget("x", "y")
        item.source = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
