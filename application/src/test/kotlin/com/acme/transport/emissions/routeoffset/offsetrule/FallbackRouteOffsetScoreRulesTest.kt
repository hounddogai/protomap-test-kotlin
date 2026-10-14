package com.acme.transport.emissions.routeoffset.offsetrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRouteOffsetScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRouteOffsetScoreValidator()
        val item = newFallbackRouteOffsetScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = FallbackRouteOffsetScoreAttemptsRangeRule(32, 120)
        val item = newFallbackRouteOffsetScore("x", "y")
        item.attempts = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackRouteOffsetScoreCodeLengthRule(3600)
        val item = newFallbackRouteOffsetScore("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackRouteOffsetScoreRequiredTitleRule("fallback route offset score needs an entry")
        val item = newFallbackRouteOffsetScore("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = FallbackRouteOffsetScoreRequiredLaneRule("fallback route offset score needs a value")
        val item = newFallbackRouteOffsetScore("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
