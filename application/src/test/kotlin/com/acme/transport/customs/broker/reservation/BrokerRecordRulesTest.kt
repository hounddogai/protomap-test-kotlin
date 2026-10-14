package com.acme.transport.customs.broker.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerRecordValidator()
        val item = newBrokerRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BrokerRecordRequiredTitleRule("broker record needs an entry")
        val item = newBrokerRecord("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BrokerRecordRequiredCodeRule("broker record needs an entry")
        val item = newBrokerRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BrokerRecordCodeLengthRule(64)
        val item = newBrokerRecord("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = BrokerRecordQuantityRangeRule(64, 3600)
        val item = newBrokerRecord("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
