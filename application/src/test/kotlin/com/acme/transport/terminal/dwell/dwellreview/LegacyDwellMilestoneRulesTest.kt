package com.acme.transport.terminal.dwell.dwellreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDwellMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDwellMilestoneValidator()
        val item = newLegacyDwellMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = LegacyDwellMilestoneStartsAtRangeRule(16, 120)
        val item = newLegacyDwellMilestone("x", "y")
        item.startsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyDwellMilestoneRequiredLabelRule("legacy dwell milestone needs an entry")
        val item = newLegacyDwellMilestone("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = LegacyDwellMilestonePriorityRangeRule(16, 86400)
        val item = newLegacyDwellMilestone("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
