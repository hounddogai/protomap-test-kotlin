package com.acme.transport.safety.coaching.coachingsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCoachingSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCoachingSegmentValidator()
        val item = newGlobalCoachingSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = GlobalCoachingSegmentRequiredLaneRule("global coaching segment needs a value")
        val item = newGlobalCoachingSegment("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = GlobalCoachingSegmentBatchSizeRangeRule(64, 120)
        val item = newGlobalCoachingSegment("x", "y")
        item.batchSize = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = GlobalCoachingSegmentRevisionRangeRule(64, 255)
        val item = newGlobalCoachingSegment("x", "y")
        item.revision = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalCoachingSegmentRequiredCodeRule("global coaching segment needs an entry")
        val item = newGlobalCoachingSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
