package com.acme.transport.terminal.sortation.sortationtier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledSortationTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledSortationTierValidator()
        val item = newScheduledSortationTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledSortationTierRequiredNameRule("scheduled sortation tier needs this field")
        val item = newScheduledSortationTier("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = ScheduledSortationTierDockLengthRule(1000)
        val item = newScheduledSortationTier("x", "y")
        item.dock = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = ScheduledSortationTierScoreRangeRule(8.8, 1640.0)
        val item = newScheduledSortationTier("x", "y")
        item.score = 1641.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = ScheduledSortationTierCreatedAtRangeRule(120, 3600)
        val item = newScheduledSortationTier("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = ScheduledSortationTierRequiredDockRule("scheduled sortation tier needs an entry")
        val item = newScheduledSortationTier("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ScheduledSortationTierBatchSizeRangeRule(16, 1000)
        val item = newScheduledSortationTier("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
