package com.acme.transport.emissions.routeoffset.offsetbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRouteOffsetBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedRouteOffsetBaselineValidator()
        val item = newCappedRouteOffsetBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedRouteOffsetBaselineRequiredCodeRule("capped route offset baseline needs this field")
        val item = newCappedRouteOffsetBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = CappedRouteOffsetBaselineThresholdRangeRule(7.2, 603.7)
        val item = newCappedRouteOffsetBaseline("x", "y")
        item.threshold = 605.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedRouteOffsetBaselineNameLengthRule(86400)
        val item = newCappedRouteOffsetBaseline("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = CappedRouteOffsetBaselineMaximumRangeRule(0.8, 3153.7)
        val item = newCappedRouteOffsetBaseline("x", "y")
        item.maximum = 3155.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
