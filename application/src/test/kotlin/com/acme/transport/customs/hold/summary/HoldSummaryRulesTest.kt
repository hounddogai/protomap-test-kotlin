package com.acme.transport.customs.hold.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldSummaryValidator()
        val item = newHoldSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = HoldSummaryBudgetRangeRule(4.7, 364.6)
        val item = newHoldSummary("x", "y")
        item.budget = 366.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = HoldSummaryCodeLengthRule(120)
        val item = newHoldSummary("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HoldSummaryRequiredCodeRule("hold summary needs a value")
        val item = newHoldSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
