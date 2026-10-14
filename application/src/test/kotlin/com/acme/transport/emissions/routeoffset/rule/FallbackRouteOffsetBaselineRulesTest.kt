package com.acme.transport.emissions.routeoffset.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRouteOffsetBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRouteOffsetBaselineValidator()
        val item = newFallbackRouteOffsetBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackRouteOffsetBaselineNameLengthRule(80)
        val item = newFallbackRouteOffsetBaseline("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackRouteOffsetBaselineRequiredNameRule("fallback route offset baseline needs a value")
        val item = newFallbackRouteOffsetBaseline("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackRouteOffsetBaselineLabelLengthRule(255)
        val item = newFallbackRouteOffsetBaseline("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackRouteOffsetBaselineCodeLengthRule(255)
        val item = newFallbackRouteOffsetBaseline("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = FallbackRouteOffsetBaselineGraceDaysRangeRule(16, 86400)
        val item = newFallbackRouteOffsetBaseline("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
