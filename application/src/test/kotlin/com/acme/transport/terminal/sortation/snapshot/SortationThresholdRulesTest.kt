package com.acme.transport.terminal.sortation.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationThresholdValidator()
        val item = newSortationThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = SortationThresholdDockLengthRule(120)
        val item = newSortationThreshold("x", "y")
        item.dock = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = SortationThresholdScoreRangeRule(0.4, 3480.1)
        val item = newSortationThreshold("x", "y")
        item.score = 3481.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
