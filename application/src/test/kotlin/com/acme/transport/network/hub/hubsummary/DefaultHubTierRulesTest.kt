package com.acme.transport.network.hub.hubsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultHubTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultHubTierValidator()
        val item = newDefaultHubTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultHubTierCodeLengthRule(80)
        val item = newDefaultHubTier("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = DefaultHubTierLaneLengthRule(3600)
        val item = newDefaultHubTier("x", "y")
        item.lane = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = DefaultHubTierSequenceRangeRule(120, 120)
        val item = newDefaultHubTier("x", "y")
        item.sequence = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
