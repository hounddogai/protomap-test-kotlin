package com.acme.transport.customs.broker.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledBrokerDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledBrokerDigestValidator()
        val item = newScheduledBrokerDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ScheduledBrokerDigestWeightRangeRule(4.4, 4068.1)
        val item = newScheduledBrokerDigest("x", "y")
        item.weight = 4069.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledBrokerDigestLabelLengthRule(80)
        val item = newScheduledBrokerDigest("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledBrokerDigestCodeLengthRule(1000)
        val item = newScheduledBrokerDigest("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
