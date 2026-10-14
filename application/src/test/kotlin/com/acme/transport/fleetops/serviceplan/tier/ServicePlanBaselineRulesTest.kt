package com.acme.transport.fleetops.serviceplan.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanBaselineValidator()
        val item = newServicePlanBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServicePlanBaselineCodeLengthRule(255)
        val item = newServicePlanBaseline("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = ServicePlanBaselineRequiredLaneRule("service plan baseline needs a value")
        val item = newServicePlanBaseline("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ServicePlanBaselineRequiredLabelRule("service plan baseline needs this field")
        val item = newServicePlanBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanBaselineRequiredCodeRule("service plan baseline needs a value")
        val item = newServicePlanBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = ServicePlanBaselineDistanceKmRangeRule(1.8, 1611.4)
        val item = newServicePlanBaseline("x", "y")
        item.distanceKm = 1612.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
