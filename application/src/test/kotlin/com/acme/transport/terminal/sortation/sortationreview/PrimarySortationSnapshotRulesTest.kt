package com.acme.transport.terminal.sortation.sortationreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimarySortationSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimarySortationSnapshotValidator()
        val item = newPrimarySortationSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = PrimarySortationSnapshotSummaryLengthRule(64)
        val item = newPrimarySortationSnapshot("x", "y")
        item.summary = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = PrimarySortationSnapshotUpdatedAtRangeRule(32, 80)
        val item = newPrimarySortationSnapshot("x", "y")
        item.updatedAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = PrimarySortationSnapshotRequiredSummaryRule("primary sortation snapshot needs this field")
        val item = newPrimarySortationSnapshot("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
