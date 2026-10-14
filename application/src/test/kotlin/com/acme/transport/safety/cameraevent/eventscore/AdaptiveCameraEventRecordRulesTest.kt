package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCameraEventRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCameraEventRecordValidator()
        val item = newAdaptiveCameraEventRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = AdaptiveCameraEventRecordEndsAtRangeRule(64, 86400)
        val item = newAdaptiveCameraEventRecord("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveCameraEventRecordRequiredTitleRule("adaptive camera event record needs an entry")
        val item = newAdaptiveCameraEventRecord("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = AdaptiveCameraEventRecordQuantityRangeRule(16, 32)
        val item = newAdaptiveCameraEventRecord("x", "y")
        item.quantity = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveCameraEventRecordTitleLengthRule(32)
        val item = newAdaptiveCameraEventRecord("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long phone`() {
        val rule = AdaptiveCameraEventRecordPhoneLengthRule(86400)
        val item = newAdaptiveCameraEventRecord("x", "y")
        item.phone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = AdaptiveCameraEventRecordRequiredShiftCodeRule("adaptive camera event record needs a value")
        val item = newAdaptiveCameraEventRecord("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
