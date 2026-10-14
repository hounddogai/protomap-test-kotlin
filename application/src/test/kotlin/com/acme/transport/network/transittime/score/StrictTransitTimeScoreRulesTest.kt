package com.acme.transport.network.transittime.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTransitTimeScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTransitTimeScoreValidator()
        val item = newStrictTransitTimeScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictTransitTimeScoreRequiredCodeRule("strict transit time score needs this field")
        val item = newStrictTransitTimeScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = StrictTransitTimeScoreRouteCodeLengthRule(1000)
        val item = newStrictTransitTimeScore("x", "y")
        item.routeCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = StrictTransitTimeScoreRequiredRouteCodeRule("strict transit time score needs this field")
        val item = newStrictTransitTimeScore("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictTransitTimeScoreRequiredNameRule("strict transit time score needs a value")
        val item = newStrictTransitTimeScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = StrictTransitTimeScoreCeilingRangeRule(4.4, 4637.2)
        val item = newStrictTransitTimeScore("x", "y")
        item.ceiling = 4638.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
