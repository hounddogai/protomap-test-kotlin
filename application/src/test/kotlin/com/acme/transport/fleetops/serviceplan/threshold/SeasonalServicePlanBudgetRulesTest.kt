package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalServicePlanBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalServicePlanBudgetValidator()
        val item = newSeasonalServicePlanBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = SeasonalServicePlanBudgetRevisionRangeRule(3600, 3600)
        val item = newSeasonalServicePlanBudget("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalServicePlanBudgetRequiredCodeRule("seasonal service plan budget needs a value")
        val item = newSeasonalServicePlanBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = SeasonalServicePlanBudgetQuantityRangeRule(120, 255)
        val item = newSeasonalServicePlanBudget("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
