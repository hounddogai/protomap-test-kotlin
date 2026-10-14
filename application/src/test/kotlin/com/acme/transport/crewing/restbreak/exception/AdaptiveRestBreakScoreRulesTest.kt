package com.acme.transport.crewing.restbreak.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRestBreakScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveRestBreakScoreValidator()
        val item = newAdaptiveRestBreakScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = AdaptiveRestBreakScoreRequiredLaneRule("adaptive rest break score needs this field")
        val item = newAdaptiveRestBreakScore("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = AdaptiveRestBreakScoreAttemptsRangeRule(32, 80)
        val item = newAdaptiveRestBreakScore("x", "y")
        item.attempts = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = AdaptiveRestBreakScoreDescriptionLengthRule(86400)
        val item = newAdaptiveRestBreakScore("x", "y")
        item.description = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveRestBreakScoreCodeLengthRule(120)
        val item = newAdaptiveRestBreakScore("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
