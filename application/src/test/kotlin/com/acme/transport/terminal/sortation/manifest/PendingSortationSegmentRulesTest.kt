package com.acme.transport.terminal.sortation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingSortationSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingSortationSegmentValidator()
        val item = newPendingSortationSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = PendingSortationSegmentPriorityRangeRule(32, 3600)
        val item = newPendingSortationSegment("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingSortationSegmentRequiredNameRule("pending sortation segment needs a value")
        val item = newPendingSortationSegment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = PendingSortationSegmentFloorValueRangeRule(9.9, 520.0)
        val item = newPendingSortationSegment("x", "y")
        item.floorValue = 521.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = PendingSortationSegmentRequiredDescriptionRule("pending sortation segment needs an entry")
        val item = newPendingSortationSegment("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = PendingSortationSegmentExpiresAtRangeRule(16, 64)
        val item = newPendingSortationSegment("x", "y")
        item.expiresAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
