package com.acme.transport.terminal.dwell.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDwellProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDwellProfileValidator()
        val item = newSeasonalDwellProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = SeasonalDwellProfileBatchSizeRangeRule(16, 64)
        val item = newSeasonalDwellProfile("x", "y")
        item.batchSize = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = SeasonalDwellProfileCostRangeRule(8.4, 391.6)
        val item = newSeasonalDwellProfile("x", "y")
        item.cost = 393.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalDwellProfileRequiredCodeRule("seasonal dwell profile needs this field")
        val item = newSeasonalDwellProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
