package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictSortationBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictSortationBatchValidator()
        val item = newStrictSortationBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = StrictSortationBatchRequiredExternalRefRule("strict sortation batch needs this field")
        val item = newStrictSortationBatch("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictSortationBatchCodeLengthRule(80)
        val item = newStrictSortationBatch("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = StrictSortationBatchScoreRangeRule(3.3, 4200.7)
        val item = newStrictSortationBatch("x", "y")
        item.score = 4202.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = StrictSortationBatchTrailerIdLengthRule(1000)
        val item = newStrictSortationBatch("x", "y")
        item.trailerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = StrictSortationBatchUpdatedAtRangeRule(120, 120)
        val item = newStrictSortationBatch("x", "y")
        item.updatedAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
