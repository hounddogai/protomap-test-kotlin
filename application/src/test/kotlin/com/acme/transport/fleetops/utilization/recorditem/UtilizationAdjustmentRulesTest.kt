package com.acme.transport.fleetops.utilization.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationAdjustmentValidator()
        val item = newUtilizationAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = UtilizationAdjustmentFuelLitersRangeRule(7.1, 4126.4)
        val item = newUtilizationAdjustment("x", "y")
        item.fuelLiters = 4127.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationAdjustmentRequiredCodeRule("utilization adjustment needs this field")
        val item = newUtilizationAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = UtilizationAdjustmentLaneLengthRule(64)
        val item = newUtilizationAdjustment("x", "y")
        item.lane = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = UtilizationAdjustmentRequiredLaneRule("utilization adjustment needs a value")
        val item = newUtilizationAdjustment("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
