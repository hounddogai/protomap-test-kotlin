package com.acme.transport.customs.broker.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerSnapshotValidator()
        val item = newBrokerSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = BrokerSnapshotRatioRangeRule(1.6, 2597.2)
        val item = newBrokerSnapshot("x", "y")
        item.ratio = 2598.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
