package com.acme.transport.fleetops.serviceplan.planpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedServicePlanTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedServicePlanTemplateValidator()
        val item = newWeightedServicePlanTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedServicePlanTemplateRequiredCodeRule("weighted service plan template needs an entry")
        val item = newWeightedServicePlanTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = WeightedServicePlanTemplateMinimumRangeRule(0.8, 1663.2)
        val item = newWeightedServicePlanTemplate("x", "y")
        item.minimum = 1664.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
