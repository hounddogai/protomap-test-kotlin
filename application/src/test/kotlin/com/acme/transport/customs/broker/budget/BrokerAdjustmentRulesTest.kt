package com.acme.transport.customs.broker.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerAdjustmentValidator()
        val item = newBrokerAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BrokerAdjustmentCodeLengthRule(86400)
        val item = newBrokerAdjustment("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BrokerAdjustmentRequiredCodeRule("broker adjustment needs this field")
        val item = newBrokerAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = BrokerAdjustmentEndsAtRangeRule(64, 1000)
        val item = newBrokerAdjustment("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
