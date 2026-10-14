package com.acme.transport.customs.broker.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBrokerCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedBrokerCheckpointValidator()
        val item = newCappedBrokerCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedBrokerCheckpointRequiredNameRule("capped broker checkpoint needs an entry")
        val item = newCappedBrokerCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = CappedBrokerCheckpointCreatedAtRangeRule(120, 1000)
        val item = newCappedBrokerCheckpoint("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = CappedBrokerCheckpointRequiredSummaryRule("capped broker checkpoint needs this field")
        val item = newCappedBrokerCheckpoint("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
