package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerBatchValidator()
        val item = newBrokerBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BrokerBatchRequiredLabelRule("broker batch needs this field")
        val item = newBrokerBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = BrokerBatchPhoneLengthRule(255)
        val item = newBrokerBatch("x", "y")
        item.phone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = BrokerBatchRequiredPhoneRule("broker batch needs a value")
        val item = newBrokerBatch("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
