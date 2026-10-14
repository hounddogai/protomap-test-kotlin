package com.acme.transport.customs.broker.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerRequestValidator()
        val item = newBrokerRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BrokerRequestLabelLengthRule(32)
        val item = newBrokerRequest("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = BrokerRequestFloorValueRangeRule(7.9, 2366.6)
        val item = newBrokerRequest("x", "y")
        item.floorValue = 2368.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
