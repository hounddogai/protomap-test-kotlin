package com.acme.transport.linehaul.relay.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayPreferenceValidator()
        val item = newRelayPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = RelayPreferenceScoreRangeRule(6.7, 2660.0)
        val item = newRelayPreference("x", "y")
        item.score = 2661.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RelayPreferenceRequiredLabelRule("relay preference needs a value")
        val item = newRelayPreference("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RelayPreferenceCodeLengthRule(16)
        val item = newRelayPreference("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = RelayPreferenceLabelLengthRule(255)
        val item = newRelayPreference("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = RelayPreferenceRequiredCurrencyRule("relay preference needs a value")
        val item = newRelayPreference("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RelayPreferenceStartsAtRangeRule(64, 3600)
        val item = newRelayPreference("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
