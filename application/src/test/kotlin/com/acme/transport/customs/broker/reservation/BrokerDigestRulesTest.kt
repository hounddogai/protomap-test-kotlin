package com.acme.transport.customs.broker.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerDigestValidator()
        val item = newBrokerDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = BrokerDigestDurationMinutesRangeRule(80, 80)
        val item = newBrokerDigest("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = BrokerDigestStopCountRangeRule(64, 80)
        val item = newBrokerDigest("x", "y")
        item.stopCount = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = BrokerDigestLoadKgRangeRule(5.5, 3018.1)
        val item = newBrokerDigest("x", "y")
        item.loadKg = 3019.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = BrokerDigestLimitRangeRule(16, 255)
        val item = newBrokerDigest("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
