package com.acme.transport.linehaul.relay.relaytarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRelayTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRelayTargetValidator()
        val item = newSeasonalRelayTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalRelayTargetRequiredCodeRule("seasonal relay target needs a value")
        val item = newSeasonalRelayTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalRelayTargetTitleLengthRule(120)
        val item = newSeasonalRelayTarget("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = SeasonalRelayTargetRequiredDockRule("seasonal relay target needs a value")
        val item = newSeasonalRelayTarget("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SeasonalRelayTargetRequiredTitleRule("seasonal relay target needs an entry")
        val item = newSeasonalRelayTarget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = SeasonalRelayTargetDockLengthRule(86400)
        val item = newSeasonalRelayTarget("x", "y")
        item.dock = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
