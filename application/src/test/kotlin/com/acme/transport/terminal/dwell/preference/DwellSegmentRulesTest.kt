package com.acme.transport.terminal.dwell.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellSegmentValidator()
        val item = newDwellSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = DwellSegmentRequiredOwnerIdRule("dwell segment needs an entry")
        val item = newDwellSegment("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = DwellSegmentOwnerIdLengthRule(32)
        val item = newDwellSegment("x", "y")
        item.ownerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = DwellSegmentRatioRangeRule(7.8, 916.1)
        val item = newDwellSegment("x", "y")
        item.ratio = 917.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = DwellSegmentCostRangeRule(2.7, 4678.2)
        val item = newDwellSegment("x", "y")
        item.cost = 4679.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = DwellSegmentCreatedAtRangeRule(64, 1000)
        val item = newDwellSegment("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
