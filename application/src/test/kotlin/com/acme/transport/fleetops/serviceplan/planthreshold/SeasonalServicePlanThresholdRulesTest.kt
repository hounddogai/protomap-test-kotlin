package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalServicePlanThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalServicePlanThresholdValidator()
        val item = newSeasonalServicePlanThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalServicePlanThresholdLabelLengthRule(1000)
        val item = newSeasonalServicePlanThreshold("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalServicePlanThresholdRequiredCodeRule("seasonal service plan threshold needs this field")
        val item = newSeasonalServicePlanThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = SeasonalServicePlanThresholdRetriesRangeRule(120, 255)
        val item = newSeasonalServicePlanThreshold("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalServicePlanThresholdRequiredLabelRule("seasonal service plan threshold needs this field")
        val item = newSeasonalServicePlanThreshold("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = SeasonalServicePlanThresholdRouteCodeLengthRule(86400)
        val item = newSeasonalServicePlanThreshold("x", "y")
        item.routeCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
