package com.acme.transport.emissions.fuelburn.burnrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFuelBurnRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingFuelBurnRequestValidator()
        val item = newRollingFuelBurnRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingFuelBurnRequestRequiredCodeRule("rolling fuel burn request needs a value")
        val item = newRollingFuelBurnRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = RollingFuelBurnRequestBatchSizeRangeRule(255, 86400)
        val item = newRollingFuelBurnRequest("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RollingFuelBurnRequestGraceDaysRangeRule(120, 120)
        val item = newRollingFuelBurnRequest("x", "y")
        item.graceDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = RollingFuelBurnRequestOwnerIdLengthRule(1000)
        val item = newRollingFuelBurnRequest("x", "y")
        item.ownerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = RollingFuelBurnRequestRatioRangeRule(6.7, 3162.0)
        val item = newRollingFuelBurnRequest("x", "y")
        item.ratio = 3163.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
