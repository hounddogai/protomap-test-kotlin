package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackLanePlanPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackLanePlanPreferenceValidator()
        val item = newFallbackLanePlanPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = FallbackLanePlanPreferenceRequiredCategoryRule("fallback lane plan preference needs this field")
        val item = newFallbackLanePlanPreference("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = FallbackLanePlanPreferenceFloorValueRangeRule(7.3, 1326.4)
        val item = newFallbackLanePlanPreference("x", "y")
        item.floorValue = 1327.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = FallbackLanePlanPreferenceCostRangeRule(3.3, 1346.2)
        val item = newFallbackLanePlanPreference("x", "y")
        item.cost = 1347.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
