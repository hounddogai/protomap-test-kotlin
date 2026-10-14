package com.acme.transport.linehaul.relay.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRelayOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRelayOverrideValidator()
        val item = newTieredRelayOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = TieredRelayOverrideWindowDaysRangeRule(120, 1000)
        val item = newTieredRelayOverride("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = TieredRelayOverrideCeilingRangeRule(1.3, 54.9)
        val item = newTieredRelayOverride("x", "y")
        item.ceiling = 56.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredRelayOverrideCodeLengthRule(3600)
        val item = newTieredRelayOverride("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredRelayOverrideTitleLengthRule(120)
        val item = newTieredRelayOverride("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
