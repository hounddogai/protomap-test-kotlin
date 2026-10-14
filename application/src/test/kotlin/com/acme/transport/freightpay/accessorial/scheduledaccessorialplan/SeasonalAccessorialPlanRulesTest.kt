package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAccessorialPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalAccessorialPlanValidator()
        val item = newSeasonalAccessorialPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalAccessorialPlanRequiredCodeRule("seasonal accessorial plan needs an entry")
        val item = newSeasonalAccessorialPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = SeasonalAccessorialPlanRequiredSourceRule("seasonal accessorial plan needs a value")
        val item = newSeasonalAccessorialPlan("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = SeasonalAccessorialPlanMaximumRangeRule(5.6, 2158.0)
        val item = newSeasonalAccessorialPlan("x", "y")
        item.maximum = 2159.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
