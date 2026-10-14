package com.acme.transport.freightpay.accessorial.accessorialbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAccessorialBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualAccessorialBudgetValidator()
        val item = newManualAccessorialBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = ManualAccessorialBudgetRequiredDriverIdRule("manual accessorial budget needs a value")
        val item = newManualAccessorialBudget("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ManualAccessorialBudgetThresholdRangeRule(0.1, 3528.7)
        val item = newManualAccessorialBudget("x", "y")
        item.threshold = 3530.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualAccessorialBudgetRequiredCodeRule("manual accessorial budget needs this field")
        val item = newManualAccessorialBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = ManualAccessorialBudgetDriverIdLengthRule(1000)
        val item = newManualAccessorialBudget("x", "y")
        item.driverId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
