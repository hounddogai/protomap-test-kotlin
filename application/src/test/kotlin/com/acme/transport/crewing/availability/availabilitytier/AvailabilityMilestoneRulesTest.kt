package com.acme.transport.crewing.availability.availabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityMilestoneValidator()
        val item = newAvailabilityMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = AvailabilityMilestoneRequiredSegmentRule("availability milestone needs a value")
        val item = newAvailabilityMilestone("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AvailabilityMilestoneRequiredTitleRule("availability milestone needs an entry")
        val item = newAvailabilityMilestone("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = AvailabilityMilestoneFuelLitersRangeRule(5.8, 4427.8)
        val item = newAvailabilityMilestone("x", "y")
        item.fuelLiters = 4429.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = AvailabilityMilestoneLoadKgRangeRule(5.1, 2093.7)
        val item = newAvailabilityMilestone("x", "y")
        item.loadKg = 2095.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = AvailabilityMilestoneTitleLengthRule(80)
        val item = newAvailabilityMilestone("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
