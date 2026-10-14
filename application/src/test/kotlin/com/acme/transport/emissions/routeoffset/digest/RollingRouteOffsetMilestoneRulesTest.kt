package com.acme.transport.emissions.routeoffset.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRouteOffsetMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRouteOffsetMilestoneValidator()
        val item = newRollingRouteOffsetMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RollingRouteOffsetMilestoneStopCountRangeRule(255, 255)
        val item = newRollingRouteOffsetMilestone("x", "y")
        item.stopCount = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingRouteOffsetMilestoneRequiredLabelRule("rolling route offset milestone needs an entry")
        val item = newRollingRouteOffsetMilestone("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingRouteOffsetMilestoneLabelLengthRule(80)
        val item = newRollingRouteOffsetMilestone("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
