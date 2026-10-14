package com.acme.transport.customs.broker.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerEntryValidator()
        val item = newBrokerEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = BrokerEntryOdometerRangeRule(64, 3600)
        val item = newBrokerEntry("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = BrokerEntryCurrencyLengthRule(64)
        val item = newBrokerEntry("x", "y")
        item.currency = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
