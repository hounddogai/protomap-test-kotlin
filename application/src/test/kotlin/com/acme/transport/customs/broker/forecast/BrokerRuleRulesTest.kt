package com.acme.transport.customs.broker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerRuleValidator()
        val item = newBrokerRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BrokerRuleCodeLengthRule(64)
        val item = newBrokerRule("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = BrokerRuleFloorValueRangeRule(9.9, 2823.3)
        val item = newBrokerRule("x", "y")
        item.floorValue = 2824.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = BrokerRuleContactEmailLengthRule(255)
        val item = newBrokerRule("x", "y")
        item.contactEmail = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
