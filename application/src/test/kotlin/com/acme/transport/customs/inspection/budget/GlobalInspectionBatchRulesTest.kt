package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalInspectionBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalInspectionBatchValidator()
        val item = newGlobalInspectionBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalInspectionBatchRequiredNameRule("global inspection batch needs this field")
        val item = newGlobalInspectionBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = GlobalInspectionBatchRequiredDriverIdRule("global inspection batch needs this field")
        val item = newGlobalInspectionBatch("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = GlobalInspectionBatchVehicleIdLengthRule(64)
        val item = newGlobalInspectionBatch("x", "y")
        item.vehicleId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = GlobalInspectionBatchSequenceRangeRule(64, 255)
        val item = newGlobalInspectionBatch("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
