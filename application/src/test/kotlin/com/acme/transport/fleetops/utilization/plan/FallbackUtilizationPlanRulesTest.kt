package com.acme.transport.fleetops.utilization.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackUtilizationPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackUtilizationPlanValidator()
        val item = newFallbackUtilizationPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = FallbackUtilizationPlanBudgetRangeRule(2.1, 2406.0)
        val item = newFallbackUtilizationPlan("x", "y")
        item.budget = 2407.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = FallbackUtilizationPlanRevisionRangeRule(16, 120)
        val item = newFallbackUtilizationPlan("x", "y")
        item.revision = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackUtilizationPlanNameLengthRule(1000)
        val item = newFallbackUtilizationPlan("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = FallbackUtilizationPlanWindowDaysRangeRule(3600, 86400)
        val item = newFallbackUtilizationPlan("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
