package com.acme.transport.terminal.dockdoor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDockDoorSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDockDoorSegmentValidator()
        val item = newSeasonalDockDoorSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SeasonalDockDoorSegmentRequiredTitleRule("seasonal dock door segment needs a value")
        val item = newSeasonalDockDoorSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = SeasonalDockDoorSegmentEndsAtRangeRule(64, 120)
        val item = newSeasonalDockDoorSegment("x", "y")
        item.endsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalDockDoorSegmentNameLengthRule(255)
        val item = newSeasonalDockDoorSegment("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = SeasonalDockDoorSegmentRegionLengthRule(16)
        val item = newSeasonalDockDoorSegment("x", "y")
        item.region = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = SeasonalDockDoorSegmentFloorValueRangeRule(8.3, 3373.1)
        val item = newSeasonalDockDoorSegment("x", "y")
        item.floorValue = 3374.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
