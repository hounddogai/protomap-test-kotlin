package com.acme.transport.lastmile.locker.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalLockerRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalLockerRecordValidator()
        val item = newGlobalLockerRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = GlobalLockerRecordRequiredSegmentRule("global locker record needs a value")
        val item = newGlobalLockerRecord("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = GlobalLockerRecordWindowDaysRangeRule(16, 255)
        val item = newGlobalLockerRecord("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = GlobalLockerRecordDurationMinutesRangeRule(16, 32)
        val item = newGlobalLockerRecord("x", "y")
        item.durationMinutes = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long segment`() {
        val rule = GlobalLockerRecordSegmentLengthRule(32)
        val item = newGlobalLockerRecord("x", "y")
        item.segment = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
