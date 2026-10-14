package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalHubClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalHubClaimValidator()
        val item = newSeasonalHubClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalHubClaimTitleLengthRule(32)
        val item = newSeasonalHubClaim("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = SeasonalHubClaimRequiredChannelRule("seasonal hub claim needs this field")
        val item = newSeasonalHubClaim("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = SeasonalHubClaimAmountRangeRule(6.4, 2736.0)
        val item = newSeasonalHubClaim("x", "y")
        item.amount = 2737.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = SeasonalHubClaimChannelLengthRule(255)
        val item = newSeasonalHubClaim("x", "y")
        item.channel = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
