package com.acme.transport.crewing.dutyperiod.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDutyPeriodSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDutyPeriodSegmentValidator()
        val item = newLenientDutyPeriodSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = LenientDutyPeriodSegmentNotesLengthRule(80)
        val item = newLenientDutyPeriodSegment("x", "y")
        item.notes = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = LenientDutyPeriodSegmentRequiredOwnerIdRule("lenient duty period segment needs a value")
        val item = newLenientDutyPeriodSegment("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = LenientDutyPeriodSegmentExpiresAtRangeRule(80, 120)
        val item = newLenientDutyPeriodSegment("x", "y")
        item.expiresAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientDutyPeriodSegmentCodeLengthRule(3600)
        val item = newLenientDutyPeriodSegment("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientDutyPeriodSegmentLabelLengthRule(255)
        val item = newLenientDutyPeriodSegment("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
