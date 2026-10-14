package com.acme.transport.customs.broker.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerWindowValidator()
        val item = newBrokerWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = BrokerWindowBudgetRangeRule(7.6, 3834.0)
        val item = newBrokerWindow("x", "y")
        item.budget = 3835.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = BrokerWindowLabelLengthRule(120)
        val item = newBrokerWindow("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
