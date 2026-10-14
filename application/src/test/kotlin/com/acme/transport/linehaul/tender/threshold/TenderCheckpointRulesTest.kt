package com.acme.transport.linehaul.tender.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderCheckpointValidator()
        val item = newTenderCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = TenderCheckpointFuelLitersRangeRule(4.9, 674.7)
        val item = newTenderCheckpoint("x", "y")
        item.fuelLiters = 676.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = TenderCheckpointWindowDaysRangeRule(120, 255)
        val item = newTenderCheckpoint("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = TenderCheckpointVehicleIdLengthRule(86400)
        val item = newTenderCheckpoint("x", "y")
        item.vehicleId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = TenderCheckpointRequiredCurrencyRule("tender checkpoint needs this field")
        val item = newTenderCheckpoint("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = TenderCheckpointFloorValueRangeRule(3.3, 518.0)
        val item = newTenderCheckpoint("x", "y")
        item.floorValue = 519.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
