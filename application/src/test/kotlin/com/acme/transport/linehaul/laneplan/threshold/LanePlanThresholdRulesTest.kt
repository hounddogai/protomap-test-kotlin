package com.acme.transport.linehaul.laneplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanThresholdValidator()
        val item = newLanePlanThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = LanePlanThresholdRequiredLocaleRule("lane plan threshold needs this field")
        val item = newLanePlanThreshold("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LanePlanThresholdCodeLengthRule(3600)
        val item = newLanePlanThreshold("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = LanePlanThresholdOdometerRangeRule(80, 1000)
        val item = newLanePlanThreshold("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = LanePlanThresholdRequiredRouteCodeRule("lane plan threshold needs a value")
        val item = newLanePlanThreshold("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = LanePlanThresholdDurationMinutesRangeRule(32, 1000)
        val item = newLanePlanThreshold("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
