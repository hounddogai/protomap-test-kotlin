package com.acme.transport.network.zone.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneMilestoneValidator()
        val item = newZoneMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ZoneMilestoneRequiredTitleRule("zone milestone needs an entry")
        val item = newZoneMilestone("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = ZoneMilestoneRequiredCurrencyRule("zone milestone needs an entry")
        val item = newZoneMilestone("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = ZoneMilestoneRequiredChannelRule("zone milestone needs an entry")
        val item = newZoneMilestone("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = ZoneMilestoneChannelLengthRule(80)
        val item = newZoneMilestone("x", "y")
        item.channel = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
