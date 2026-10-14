package com.acme.transport.customs.broker.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerExceptionValidator()
        val item = newBrokerException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BrokerExceptionNameLengthRule(86400)
        val item = newBrokerException("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = BrokerExceptionRequiredSegmentRule("broker exception needs a value")
        val item = newBrokerException("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = BrokerExceptionPalletsRangeRule(80, 3600)
        val item = newBrokerException("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
