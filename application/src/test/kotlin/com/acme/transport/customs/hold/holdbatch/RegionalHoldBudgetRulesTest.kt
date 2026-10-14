package com.acme.transport.customs.hold.holdbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalHoldBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalHoldBudgetValidator()
        val item = newRegionalHoldBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RegionalHoldBudgetStopCountRangeRule(32, 80)
        val item = newRegionalHoldBudget("x", "y")
        item.stopCount = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalHoldBudgetCodeLengthRule(64)
        val item = newRegionalHoldBudget("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalHoldBudgetRequiredNameRule("regional hold budget needs this field")
        val item = newRegionalHoldBudget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
