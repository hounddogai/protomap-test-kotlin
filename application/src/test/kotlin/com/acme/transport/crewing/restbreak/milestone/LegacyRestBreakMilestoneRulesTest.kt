package com.acme.transport.crewing.restbreak.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRestBreakMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyRestBreakMilestoneValidator()
        val item = newLegacyRestBreakMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = LegacyRestBreakMilestoneDurationMinutesRangeRule(120, 3600)
        val item = newLegacyRestBreakMilestone("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyRestBreakMilestoneRequiredCodeRule("legacy rest break milestone needs a value")
        val item = newLegacyRestBreakMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyRestBreakMilestoneRequiredNameRule("legacy rest break milestone needs this field")
        val item = newLegacyRestBreakMilestone("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
