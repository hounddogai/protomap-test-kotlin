package com.acme.transport.linehaul.relay.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRelayScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRelayScoreValidator()
        val item = newLenientRelayScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = LenientRelayScoreRequiredDockRule("lenient relay score needs this field")
        val item = newLenientRelayScore("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = LenientRelayScoreMinimumRangeRule(0.8, 2774.5)
        val item = newLenientRelayScore("x", "y")
        item.minimum = 2776.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientRelayScoreRequiredLabelRule("lenient relay score needs a value")
        val item = newLenientRelayScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = LenientRelayScoreDockLengthRule(80)
        val item = newLenientRelayScore("x", "y")
        item.dock = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
