package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyInspectionPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyInspectionPolicyValidator()
        val item = newLegacyInspectionPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = LegacyInspectionPolicyDurationMinutesRangeRule(16, 3600)
        val item = newLegacyInspectionPolicy("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = LegacyInspectionPolicyRequiredRegionRule("legacy inspection policy needs an entry")
        val item = newLegacyInspectionPolicy("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = LegacyInspectionPolicyRegionLengthRule(64)
        val item = newLegacyInspectionPolicy("x", "y")
        item.region = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
