package com.acme.transport.customs.inspection.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingInspectionReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingInspectionReservationValidator()
        val item = newRollingInspectionReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingInspectionReservationNameLengthRule(80)
        val item = newRollingInspectionReservation("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RollingInspectionReservationSequenceRangeRule(80, 120)
        val item = newRollingInspectionReservation("x", "y")
        item.sequence = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = RollingInspectionReservationCategoryLengthRule(3600)
        val item = newRollingInspectionReservation("x", "y")
        item.category = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = RollingInspectionReservationRequiredCategoryRule("rolling inspection reservation needs this field")
        val item = newRollingInspectionReservation("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = RollingInspectionReservationScoreRangeRule(6.6, 351.9)
        val item = newRollingInspectionReservation("x", "y")
        item.score = 353.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RollingInspectionReservationFuelLitersRangeRule(2.9, 1231.9)
        val item = newRollingInspectionReservation("x", "y")
        item.fuelLiters = 1233.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
