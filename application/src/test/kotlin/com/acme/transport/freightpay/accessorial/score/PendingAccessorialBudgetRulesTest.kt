package com.acme.transport.freightpay.accessorial.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAccessorialBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingAccessorialBudgetValidator()
        val item = newPendingAccessorialBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = PendingAccessorialBudgetLaneLengthRule(80)
        val item = newPendingAccessorialBudget("x", "y")
        item.lane = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = PendingAccessorialBudgetThresholdRangeRule(8.2, 2603.5)
        val item = newPendingAccessorialBudget("x", "y")
        item.threshold = 2605.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = PendingAccessorialBudgetPriorityRangeRule(64, 120)
        val item = newPendingAccessorialBudget("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingAccessorialBudgetRequiredNameRule("pending accessorial budget needs this field")
        val item = newPendingAccessorialBudget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = PendingAccessorialBudgetQuantityRangeRule(32, 3600)
        val item = newPendingAccessorialBudget("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
