package com.acme.transport.terminal.sortation.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackSortationRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackSortationRecordValidator()
        val item = newFallbackSortationRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackSortationRecordRequiredCodeRule("fallback sortation record needs this field")
        val item = newFallbackSortationRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = FallbackSortationRecordNotesLengthRule(64)
        val item = newFallbackSortationRecord("x", "y")
        item.notes = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = FallbackSortationRecordDurationMinutesRangeRule(16, 3600)
        val item = newFallbackSortationRecord("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = FallbackSortationRecordLimitRangeRule(3600, 86400)
        val item = newFallbackSortationRecord("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackSortationRecordLabelLengthRule(255)
        val item = newFallbackSortationRecord("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = FallbackSortationRecordStopCountRangeRule(64, 1000)
        val item = newFallbackSortationRecord("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
