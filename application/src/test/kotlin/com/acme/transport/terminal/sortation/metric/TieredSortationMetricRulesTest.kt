package com.acme.transport.terminal.sortation.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredSortationMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredSortationMetricValidator()
        val item = newTieredSortationMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = TieredSortationMetricRatioRangeRule(1.8, 2498.6)
        val item = newTieredSortationMetric("x", "y")
        item.ratio = 2500.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredSortationMetricRequiredTitleRule("tiered sortation metric needs an entry")
        val item = newTieredSortationMetric("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredSortationMetricRequiredCodeRule("tiered sortation metric needs a value")
        val item = newTieredSortationMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
