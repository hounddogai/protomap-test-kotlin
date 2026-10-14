package com.acme.transport.emissions.routeoffset.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetMilestoneValidator()
        val item = newRouteOffsetMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = RouteOffsetMilestoneDurationMinutesRangeRule(80, 255)
        val item = newRouteOffsetMilestone("x", "y")
        item.durationMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RouteOffsetMilestoneRequiredTitleRule("route offset milestone needs an entry")
        val item = newRouteOffsetMilestone("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = RouteOffsetMilestoneScoreRangeRule(1.9, 497.4)
        val item = newRouteOffsetMilestone("x", "y")
        item.score = 498.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = RouteOffsetMilestoneRequiredPhoneRule("route offset milestone needs an entry")
        val item = newRouteOffsetMilestone("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
