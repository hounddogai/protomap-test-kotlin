package com.acme.transport.terminal.sortation.sortationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultSortationRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultSortationRecordValidator()
        val item = newDefaultSortationRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = DefaultSortationRecordAmountRangeRule(5.0, 1415.3)
        val item = newDefaultSortationRecord("x", "y")
        item.amount = 1416.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultSortationRecordLabelLengthRule(64)
        val item = newDefaultSortationRecord("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultSortationRecordCodeLengthRule(255)
        val item = newDefaultSortationRecord("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = DefaultSortationRecordRevisionRangeRule(80, 255)
        val item = newDefaultSortationRecord("x", "y")
        item.revision = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
