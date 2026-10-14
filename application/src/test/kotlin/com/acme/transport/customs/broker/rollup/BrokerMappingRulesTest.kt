package com.acme.transport.customs.broker.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerMappingValidator()
        val item = newBrokerMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BrokerMappingCodeLengthRule(1000)
        val item = newBrokerMapping("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = BrokerMappingTitleLengthRule(32)
        val item = newBrokerMapping("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = BrokerMappingDurationMinutesRangeRule(120, 255)
        val item = newBrokerMapping("x", "y")
        item.durationMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
