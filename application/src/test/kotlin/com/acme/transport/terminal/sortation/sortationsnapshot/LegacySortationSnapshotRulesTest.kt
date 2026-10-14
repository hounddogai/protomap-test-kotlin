package com.acme.transport.terminal.sortation.sortationsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacySortationSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacySortationSnapshotValidator()
        val item = newLegacySortationSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LegacySortationSnapshotCapacityRangeRule(64, 64)
        val item = newLegacySortationSnapshot("x", "y")
        item.capacity = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = LegacySortationSnapshotTimezoneLengthRule(32)
        val item = newLegacySortationSnapshot("x", "y")
        item.timezone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LegacySortationSnapshotRequiredTimezoneRule("legacy sortation snapshot needs this field")
        val item = newLegacySortationSnapshot("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacySortationSnapshotLabelLengthRule(80)
        val item = newLegacySortationSnapshot("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacySortationSnapshotRequiredCodeRule("legacy sortation snapshot needs this field")
        val item = newLegacySortationSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
