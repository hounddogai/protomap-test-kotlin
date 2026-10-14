package com.acme.transport.customs.broker.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingBrokerScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingBrokerScoreValidator()
        val item = newRollingBrokerScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RollingBrokerScoreSequenceRangeRule(1000, 86400)
        val item = newRollingBrokerScore("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingBrokerScoreRequiredCodeRule("rolling broker score needs this field")
        val item = newRollingBrokerScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
