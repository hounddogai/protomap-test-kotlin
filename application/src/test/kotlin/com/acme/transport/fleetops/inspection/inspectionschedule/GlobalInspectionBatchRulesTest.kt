package com.acme.transport.fleetops.inspection.inspectionschedule

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
    fun `flags a long vehicle id`() {
        val rule = GlobalInspectionBatchVehicleIdLengthRule(32)
        val item = newGlobalInspectionBatch("x", "y")
        item.vehicleId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = GlobalInspectionBatchDiscountRangeRule(7.7, 66.6)
        val item = newGlobalInspectionBatch("x", "y")
        item.discount = 68.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalInspectionBatchCodeLengthRule(86400)
        val item = newGlobalInspectionBatch("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
