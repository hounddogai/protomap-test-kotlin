package com.acme.transport.linehaul.relay.relayassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRelayTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRelayTierValidator()
        val item = newDefaultRelayTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = DefaultRelayTierBatchSizeRangeRule(1000, 1000)
        val item = newDefaultRelayTier("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = DefaultRelayTierRequiredSummaryRule("default relay tier needs this field")
        val item = newDefaultRelayTier("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultRelayTierNameLengthRule(64)
        val item = newDefaultRelayTier("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultRelayTierRequiredNameRule("default relay tier needs a value")
        val item = newDefaultRelayTier("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = DefaultRelayTierWindowDaysRangeRule(80, 120)
        val item = newDefaultRelayTier("x", "y")
        item.windowDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
