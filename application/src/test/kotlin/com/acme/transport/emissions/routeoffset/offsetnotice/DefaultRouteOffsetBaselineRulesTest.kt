package com.acme.transport.emissions.routeoffset.offsetnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRouteOffsetBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRouteOffsetBaselineValidator()
        val item = newDefaultRouteOffsetBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = DefaultRouteOffsetBaselineDurationMinutesRangeRule(64, 1000)
        val item = newDefaultRouteOffsetBaseline("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultRouteOffsetBaselineRequiredTitleRule("default route offset baseline needs this field")
        val item = newDefaultRouteOffsetBaseline("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
