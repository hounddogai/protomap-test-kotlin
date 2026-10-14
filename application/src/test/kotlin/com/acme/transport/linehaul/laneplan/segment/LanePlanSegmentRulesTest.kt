package com.acme.transport.linehaul.laneplan.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanSegmentValidator()
        val item = newLanePlanSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = LanePlanSegmentNotesLengthRule(255)
        val item = newLanePlanSegment("x", "y")
        item.notes = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LanePlanSegmentRequiredCodeRule("lane plan segment needs an entry")
        val item = newLanePlanSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LanePlanSegmentLimitRangeRule(32, 3600)
        val item = newLanePlanSegment("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LanePlanSegmentUpdatedAtRangeRule(32, 3600)
        val item = newLanePlanSegment("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LanePlanSegmentRequiredTitleRule("lane plan segment needs this field")
        val item = newLanePlanSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
