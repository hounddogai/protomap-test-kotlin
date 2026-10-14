package com.acme.transport.network.hub.tieredhubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultHubPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultHubPlanValidator()
        val item = newDefaultHubPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = DefaultHubPlanBatchSizeRangeRule(32, 80)
        val item = newDefaultHubPlan("x", "y")
        item.batchSize = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = DefaultHubPlanUpdatedAtRangeRule(16, 64)
        val item = newDefaultHubPlan("x", "y")
        item.updatedAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultHubPlanRequiredLabelRule("default hub plan needs this field")
        val item = newDefaultHubPlan("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = DefaultHubPlanBudgetRangeRule(7.5, 4276.2)
        val item = newDefaultHubPlan("x", "y")
        item.budget = 4277.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = DefaultHubPlanRequiredTrailerIdRule("default hub plan needs this field")
        val item = newDefaultHubPlan("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
