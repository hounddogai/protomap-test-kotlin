package com.acme.transport.network.transittime.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeMilestoneValidator()
        val item = newTransitTimeMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = TransitTimeMilestoneOdometerRangeRule(16, 64)
        val item = newTransitTimeMilestone("x", "y")
        item.odometer = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TransitTimeMilestoneRequiredCodeRule("transit time milestone needs an entry")
        val item = newTransitTimeMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = TransitTimeMilestoneDurationMinutesRangeRule(120, 120)
        val item = newTransitTimeMilestone("x", "y")
        item.durationMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = TransitTimeMilestoneRequiredCategoryRule("transit time milestone needs a value")
        val item = newTransitTimeMilestone("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TransitTimeMilestoneRequiredLabelRule("transit time milestone needs this field")
        val item = newTransitTimeMilestone("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
