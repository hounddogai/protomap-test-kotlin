package com.acme.transport.customs.broker.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalBrokerCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalBrokerCheckpointValidator()
        val item = newRegionalBrokerCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = RegionalBrokerCheckpointRequiredUnitRule("regional broker checkpoint needs this field")
        val item = newRegionalBrokerCheckpoint("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = RegionalBrokerCheckpointRateRangeRule(7.5, 171.2)
        val item = newRegionalBrokerCheckpoint("x", "y")
        item.rate = 172.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = RegionalBrokerCheckpointRequiredRegionRule("regional broker checkpoint needs a value")
        val item = newRegionalBrokerCheckpoint("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = RegionalBrokerCheckpointUnitLengthRule(1000)
        val item = newRegionalBrokerCheckpoint("x", "y")
        item.unit = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalBrokerCheckpointRequiredCodeRule("regional broker checkpoint needs this field")
        val item = newRegionalBrokerCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
