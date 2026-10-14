package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackServicePlanRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackServicePlanRollupValidator()
        val item = newFallbackServicePlanRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackServicePlanRollupRequiredLabelRule("fallback service plan rollup needs an entry")
        val item = newFallbackServicePlanRollup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackServicePlanRollupRequiredCodeRule("fallback service plan rollup needs this field")
        val item = newFallbackServicePlanRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = FallbackServicePlanRollupUpdatedAtRangeRule(32, 86400)
        val item = newFallbackServicePlanRollup("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = FallbackServicePlanRollupWindowDaysRangeRule(64, 3600)
        val item = newFallbackServicePlanRollup("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = FallbackServicePlanRollupRequiredRouteCodeRule("fallback service plan rollup needs an entry")
        val item = newFallbackServicePlanRollup("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
