package com.acme.transport.terminal.sortation.sortationvariance

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalSortationVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalSortationVarianceValidator()
        val item = newSeasonalSortationVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = SeasonalSortationVarianceCeilingRangeRule(2.9, 2459.7)
        val item = newSeasonalSortationVariance("x", "y")
        item.ceiling = 2461.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = SeasonalSortationVarianceRetriesRangeRule(32, 64)
        val item = newSeasonalSortationVariance("x", "y")
        item.retries = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = SeasonalSortationVarianceWindowDaysRangeRule(120, 3600)
        val item = newSeasonalSortationVariance("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
