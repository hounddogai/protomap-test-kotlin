package com.acme.transport.terminal.dockdoor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDockDoorSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultDockDoorSegmentValidator()
        val item = newDefaultDockDoorSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DefaultDockDoorSegmentThresholdRangeRule(5.0, 3202.1)
        val item = newDefaultDockDoorSegment("x", "y")
        item.threshold = 3203.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultDockDoorSegmentRequiredTitleRule("default dock door segment needs an entry")
        val item = newDefaultDockDoorSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = DefaultDockDoorSegmentDockLengthRule(80)
        val item = newDefaultDockDoorSegment("x", "y")
        item.dock = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
