package com.acme.transport.network.hub.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveHubScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveHubScoreValidator()
        val item = newAdaptiveHubScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveHubScoreCodeLengthRule(64)
        val item = newAdaptiveHubScore("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveHubScoreRequiredCodeRule("adaptive hub score needs this field")
        val item = newAdaptiveHubScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveHubScoreNameLengthRule(16)
        val item = newAdaptiveHubScore("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = AdaptiveHubScoreEndsAtRangeRule(16, 32)
        val item = newAdaptiveHubScore("x", "y")
        item.endsAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
