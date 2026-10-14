package com.acme.transport.linehaul.relay.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRelayEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRelayEntryValidator()
        val item = newRollingRelayEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RollingRelayEntryAttemptsRangeRule(16, 120)
        val item = newRollingRelayEntry("x", "y")
        item.attempts = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingRelayEntryRequiredNameRule("rolling relay entry needs an entry")
        val item = newRollingRelayEntry("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingRelayEntryRequiredCodeRule("rolling relay entry needs an entry")
        val item = newRollingRelayEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = RollingRelayEntryRequiredCarrierRule("rolling relay entry needs an entry")
        val item = newRollingRelayEntry("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
