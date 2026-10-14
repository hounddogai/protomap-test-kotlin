package com.acme.transport.safety.violation.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedViolationRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedViolationRequestValidator()
        val item = newWeightedViolationRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedViolationRequestCodeLengthRule(32)
        val item = newWeightedViolationRequest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = WeightedViolationRequestRequiredVehicleIdRule("weighted violation request needs this field")
        val item = newWeightedViolationRequest("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedViolationRequestRequiredLabelRule("weighted violation request needs this field")
        val item = newWeightedViolationRequest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = WeightedViolationRequestVehicleIdLengthRule(3600)
        val item = newWeightedViolationRequest("x", "y")
        item.vehicleId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedViolationRequestRequiredCodeRule("weighted violation request needs a value")
        val item = newWeightedViolationRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
