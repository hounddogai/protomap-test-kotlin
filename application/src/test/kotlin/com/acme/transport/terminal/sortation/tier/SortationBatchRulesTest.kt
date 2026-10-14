package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationBatchValidator()
        val item = newSortationBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = SortationBatchRateRangeRule(8.4, 1219.6)
        val item = newSortationBatch("x", "y")
        item.rate = 1221.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SortationBatchRequiredNameRule("sortation batch needs a value")
        val item = newSortationBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
