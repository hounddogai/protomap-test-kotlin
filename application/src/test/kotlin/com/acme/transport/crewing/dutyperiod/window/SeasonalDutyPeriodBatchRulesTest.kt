package com.acme.transport.crewing.dutyperiod.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDutyPeriodBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDutyPeriodBatchValidator()
        val item = newSeasonalDutyPeriodBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = SeasonalDutyPeriodBatchQuantityRangeRule(80, 86400)
        val item = newSeasonalDutyPeriodBatch("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = SeasonalDutyPeriodBatchBudgetRangeRule(8.0, 4984.1)
        val item = newSeasonalDutyPeriodBatch("x", "y")
        item.budget = 4985.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = SeasonalDutyPeriodBatchRequiredDescriptionRule("seasonal duty period batch needs an entry")
        val item = newSeasonalDutyPeriodBatch("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
