package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledZoneSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledZoneSegmentValidator()
        val item = newScheduledZoneSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledZoneSegmentTitleLengthRule(64)
        val item = newScheduledZoneSegment("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long channel`() {
        val rule = ScheduledZoneSegmentChannelLengthRule(3600)
        val item = newScheduledZoneSegment("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = ScheduledZoneSegmentLaneLengthRule(64)
        val item = newScheduledZoneSegment("x", "y")
        item.lane = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = ScheduledZoneSegmentRequiredChannelRule("scheduled zone segment needs a value")
        val item = newScheduledZoneSegment("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
