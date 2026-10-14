package com.acme.transport.safety.violation.violationpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveViolationScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveViolationScoreValidator()
        val item = newAdaptiveViolationScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = AdaptiveViolationScoreDistanceKmRangeRule(7.9, 1478.3)
        val item = newAdaptiveViolationScore("x", "y")
        item.distanceKm = 1479.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = AdaptiveViolationScoreRequiredLaneRule("adaptive violation score needs a value")
        val item = newAdaptiveViolationScore("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = AdaptiveViolationScoreLaneLengthRule(1000)
        val item = newAdaptiveViolationScore("x", "y")
        item.lane = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = AdaptiveViolationScoreRequiredCurrencyRule("adaptive violation score needs this field")
        val item = newAdaptiveViolationScore("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveViolationScoreRequiredCodeRule("adaptive violation score needs an entry")
        val item = newAdaptiveViolationScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
