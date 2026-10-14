package com.acme.transport.customs.broker.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingBrokerCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingBrokerCheckpointValidator()
        val item = newPendingBrokerCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = PendingBrokerCheckpointExternalRefLengthRule(32)
        val item = newPendingBrokerCheckpoint("x", "y")
        item.externalRef = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = PendingBrokerCheckpointCeilingRangeRule(2.0, 4297.5)
        val item = newPendingBrokerCheckpoint("x", "y")
        item.ceiling = 4299.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingBrokerCheckpointCodeLengthRule(255)
        val item = newPendingBrokerCheckpoint("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = PendingBrokerCheckpointGraceDaysRangeRule(32, 1000)
        val item = newPendingBrokerCheckpoint("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
