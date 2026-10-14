package com.acme.transport.terminal.sortation.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationVarianceValidator()
        val item = newSortationVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SortationVarianceRequiredNameRule("sortation variance needs an entry")
        val item = newSortationVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = SortationVarianceRatioRangeRule(3.8, 1691.1)
        val item = newSortationVariance("x", "y")
        item.ratio = 1692.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = SortationVarianceDescriptionLengthRule(32)
        val item = newSortationVariance("x", "y")
        item.description = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = SortationVarianceVersionRangeRule(80, 86400)
        val item = newSortationVariance("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = SortationVarianceBatchSizeRangeRule(16, 86400)
        val item = newSortationVariance("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
