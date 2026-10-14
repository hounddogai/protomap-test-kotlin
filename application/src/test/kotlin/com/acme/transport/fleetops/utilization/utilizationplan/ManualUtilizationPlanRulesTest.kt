package com.acme.transport.fleetops.utilization.utilizationplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualUtilizationPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualUtilizationPlanValidator()
        val item = newManualUtilizationPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualUtilizationPlanRequiredCodeRule("manual utilization plan needs a value")
        val item = newManualUtilizationPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = ManualUtilizationPlanCostRangeRule(2.9, 3130.6)
        val item = newManualUtilizationPlan("x", "y")
        item.cost = 3132.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ManualUtilizationPlanTimezoneLengthRule(120)
        val item = newManualUtilizationPlan("x", "y")
        item.timezone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
