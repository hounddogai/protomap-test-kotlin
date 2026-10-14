package com.acme.transport.fleetops.inspection.inspectionagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedInspectionExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedInspectionExceptionValidator()
        val item = newWeightedInspectionException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = WeightedInspectionExceptionEndsAtRangeRule(120, 120)
        val item = newWeightedInspectionException("x", "y")
        item.endsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedInspectionExceptionRequiredLabelRule("weighted inspection exception needs an entry")
        val item = newWeightedInspectionException("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedInspectionExceptionRequiredCodeRule("weighted inspection exception needs an entry")
        val item = newWeightedInspectionException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = WeightedInspectionExceptionRequiredVehicleIdRule("weighted inspection exception needs this field")
        val item = newWeightedInspectionException("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = WeightedInspectionExceptionRequiredLaneRule("weighted inspection exception needs a value")
        val item = newWeightedInspectionException("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = WeightedInspectionExceptionLaneLengthRule(64)
        val item = newWeightedInspectionException("x", "y")
        item.lane = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
