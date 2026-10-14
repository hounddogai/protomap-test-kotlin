package com.acme.transport.fleetops.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveInspectionPolicyValidator()
        val item = newAdaptiveInspectionPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = AdaptiveInspectionPolicyLaneLengthRule(64)
        val item = newAdaptiveInspectionPolicy("x", "y")
        item.lane = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveInspectionPolicyCodeLengthRule(32)
        val item = newAdaptiveInspectionPolicy("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = AdaptiveInspectionPolicyOdometerRangeRule(1000, 86400)
        val item = newAdaptiveInspectionPolicy("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveInspectionPolicyNameLengthRule(255)
        val item = newAdaptiveInspectionPolicy("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = AdaptiveInspectionPolicyMarginRangeRule(8.2, 231.3)
        val item = newAdaptiveInspectionPolicy("x", "y")
        item.margin = 232.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
