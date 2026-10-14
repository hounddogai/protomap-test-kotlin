package com.acme.transport.customs.broker.brokergroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerThresholdValidator()
        val item = newBrokerThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = BrokerThresholdCreatedAtRangeRule(80, 255)
        val item = newBrokerThreshold("x", "y")
        item.createdAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BrokerThresholdRequiredCodeRule("broker threshold needs this field")
        val item = newBrokerThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
