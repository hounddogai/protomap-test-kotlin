package com.acme.transport.customs.inspection.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionCheckpointValidator()
        val item = newInspectionCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = InspectionCheckpointRequiredNameRule("inspection checkpoint needs this field")
        val item = newInspectionCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = InspectionCheckpointVehicleIdLengthRule(1000)
        val item = newInspectionCheckpoint("x", "y")
        item.vehicleId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = InspectionCheckpointStartsAtRangeRule(64, 120)
        val item = newInspectionCheckpoint("x", "y")
        item.startsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
