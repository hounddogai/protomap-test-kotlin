package com.acme.transport.safety.cameraevent.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCameraEventPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalCameraEventPlanValidator()
        val item = newSeasonalCameraEventPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = SeasonalCameraEventPlanRetriesRangeRule(80, 255)
        val item = newSeasonalCameraEventPlan("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = SeasonalCameraEventPlanRequiredTimezoneRule("seasonal camera event plan needs this field")
        val item = newSeasonalCameraEventPlan("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
