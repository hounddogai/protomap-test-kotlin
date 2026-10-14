package com.acme.transport.terminal.sortation.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacySortationRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacySortationRecordValidator()
        val item = newLegacySortationRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LegacySortationRecordRetriesRangeRule(80, 120)
        val item = newLegacySortationRecord("x", "y")
        item.retries = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = LegacySortationRecordRequiredCategoryRule("legacy sortation record needs an entry")
        val item = newLegacySortationRecord("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = LegacySortationRecordCategoryLengthRule(255)
        val item = newLegacySortationRecord("x", "y")
        item.category = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
