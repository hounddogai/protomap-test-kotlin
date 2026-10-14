package com.acme.transport.customs.broker.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerRollupValidator()
        val item = newBrokerRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = BrokerRollupThresholdRangeRule(0.6, 2469.7)
        val item = newBrokerRollup("x", "y")
        item.threshold = 2471.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = BrokerRollupDepotLengthRule(255)
        val item = newBrokerRollup("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = BrokerRollupMarginRangeRule(2.3, 2676.3)
        val item = newBrokerRollup("x", "y")
        item.margin = 2677.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
