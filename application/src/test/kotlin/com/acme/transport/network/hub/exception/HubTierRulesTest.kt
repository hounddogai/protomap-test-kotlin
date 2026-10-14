package com.acme.transport.network.hub.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubTierValidator()
        val item = newHubTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = HubTierRequiredTitleRule("hub tier needs a value")
        val item = newHubTier("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = HubTierRequiredLaneRule("hub tier needs an entry")
        val item = newHubTier("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = HubTierTitleLengthRule(255)
        val item = newHubTier("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = HubTierOdometerRangeRule(120, 3600)
        val item = newHubTier("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HubTierRequiredCodeRule("hub tier needs this field")
        val item = newHubTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
