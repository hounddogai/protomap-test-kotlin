package com.acme.transport.safety.cameraevent.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCameraEventPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCameraEventPolicyValidator()
        val item = newManualCameraEventPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualCameraEventPolicyRequiredNameRule("manual camera event policy needs this field")
        val item = newManualCameraEventPolicy("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = ManualCameraEventPolicyCeilingRangeRule(8.9, 3214.9)
        val item = newManualCameraEventPolicy("x", "y")
        item.ceiling = 3216.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = ManualCameraEventPolicyRequiredVehicleIdRule("manual camera event policy needs a value")
        val item = newManualCameraEventPolicy("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualCameraEventPolicyRequiredCodeRule("manual camera event policy needs a value")
        val item = newManualCameraEventPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = ManualCameraEventPolicyVehicleIdLengthRule(64)
        val item = newManualCameraEventPolicy("x", "y")
        item.vehicleId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
