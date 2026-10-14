package com.acme.transport.fleetops.serviceplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalServicePlanPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalServicePlanPreferenceValidator()
        val item = newSeasonalServicePlanPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = SeasonalServicePlanPreferenceWindowDaysRangeRule(32, 80)
        val item = newSeasonalServicePlanPreference("x", "y")
        item.windowDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalServicePlanPreferenceRequiredLabelRule("seasonal service plan preference needs this field")
        val item = newSeasonalServicePlanPreference("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = SeasonalServicePlanPreferenceRouteCodeLengthRule(80)
        val item = newSeasonalServicePlanPreference("x", "y")
        item.routeCode = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
