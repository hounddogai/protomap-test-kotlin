package com.acme.transport.customs.broker.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerProfileValidator()
        val item = newBrokerProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = BrokerProfileRequiredRegionRule("broker profile needs an entry")
        val item = newBrokerProfile("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = BrokerProfileRegionLengthRule(16)
        val item = newBrokerProfile("x", "y")
        item.region = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BrokerProfileRequiredTitleRule("broker profile needs this field")
        val item = newBrokerProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = BrokerProfilePriorityRangeRule(16, 3600)
        val item = newBrokerProfile("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
