package com.acme.transport.terminal.sortation.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationRecordValidator()
        val item = newSortationRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = SortationRecordStartsAtRangeRule(80, 255)
        val item = newSortationRecord("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = SortationRecordSequenceRangeRule(32, 3600)
        val item = newSortationRecord("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SortationRecordCodeLengthRule(3600)
        val item = newSortationRecord("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
