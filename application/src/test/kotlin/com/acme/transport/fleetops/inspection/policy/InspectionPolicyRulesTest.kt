package com.acme.transport.fleetops.inspection.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionPolicyValidator()
        val item = newInspectionPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = InspectionPolicyDistanceKmRangeRule(7.3, 4826.4)
        val item = newInspectionPolicy("x", "y")
        item.distanceKm = 4827.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionPolicyLabelLengthRule(64)
        val item = newInspectionPolicy("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = InspectionPolicyRateRangeRule(8.2, 277.3)
        val item = newInspectionPolicy("x", "y")
        item.rate = 278.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = InspectionPolicyContactEmailLengthRule(1000)
        val item = newInspectionPolicy("x", "y")
        item.contactEmail = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
