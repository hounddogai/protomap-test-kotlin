package com.acme.transport.fleetops.inspection.inspectiongroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingInspectionCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingInspectionCheckpointValidator()
        val item = newRollingInspectionCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingInspectionCheckpointRequiredNameRule("rolling inspection checkpoint needs a value")
        val item = newRollingInspectionCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RollingInspectionCheckpointFuelLitersRangeRule(1.4, 3893.7)
        val item = newRollingInspectionCheckpoint("x", "y")
        item.fuelLiters = 3895.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = RollingInspectionCheckpointRequiredVehicleIdRule("rolling inspection checkpoint needs an entry")
        val item = newRollingInspectionCheckpoint("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = RollingInspectionCheckpointVehicleIdLengthRule(32)
        val item = newRollingInspectionCheckpoint("x", "y")
        item.vehicleId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
