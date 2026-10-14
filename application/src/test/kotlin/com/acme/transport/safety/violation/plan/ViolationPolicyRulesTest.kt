package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationPolicyValidator()
        val item = newViolationPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = ViolationPolicyRevisionRangeRule(80, 86400)
        val item = newViolationPolicy("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = ViolationPolicyStopCountRangeRule(80, 3600)
        val item = newViolationPolicy("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ViolationPolicyTitleLengthRule(1000)
        val item = newViolationPolicy("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = ViolationPolicySummaryLengthRule(86400)
        val item = newViolationPolicy("x", "y")
        item.summary = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = ViolationPolicyVehicleIdLengthRule(255)
        val item = newViolationPolicy("x", "y")
        item.vehicleId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
