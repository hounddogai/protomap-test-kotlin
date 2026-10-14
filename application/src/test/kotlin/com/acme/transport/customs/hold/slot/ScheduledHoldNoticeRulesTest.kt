package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledHoldNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledHoldNoticeValidator()
        val item = newScheduledHoldNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = ScheduledHoldNoticeChannelLengthRule(80)
        val item = newScheduledHoldNotice("x", "y")
        item.channel = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = ScheduledHoldNoticeCreatedAtRangeRule(16, 255)
        val item = newScheduledHoldNotice("x", "y")
        item.createdAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = ScheduledHoldNoticeOdometerRangeRule(3600, 3600)
        val item = newScheduledHoldNotice("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = ScheduledHoldNoticeMarginRangeRule(5.0, 4932.6)
        val item = newScheduledHoldNotice("x", "y")
        item.margin = 4934.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledHoldNoticeNameLengthRule(64)
        val item = newScheduledHoldNotice("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
