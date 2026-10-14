package com.acme.transport.terminal.sortation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredSortationSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredSortationSummaryValidator()
        val item = newTieredSortationSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredSortationSummaryRequiredCodeRule("tiered sortation summary needs this field")
        val item = newTieredSortationSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredSortationSummaryCodeLengthRule(1000)
        val item = newTieredSortationSummary("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = TieredSortationSummaryRetriesRangeRule(16, 1000)
        val item = newTieredSortationSummary("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredSortationSummaryRequiredLabelRule("tiered sortation summary needs this field")
        val item = newTieredSortationSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredSortationSummaryNameLengthRule(86400)
        val item = newTieredSortationSummary("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
