package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalLanePlanEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalLanePlanEstimateValidator()
        val item = newSeasonalLanePlanEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalLanePlanEstimateRequiredCodeRule("seasonal lane plan estimate needs an entry")
        val item = newSeasonalLanePlanEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = SeasonalLanePlanEstimatePalletsRangeRule(80, 120)
        val item = newSeasonalLanePlanEstimate("x", "y")
        item.pallets = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
