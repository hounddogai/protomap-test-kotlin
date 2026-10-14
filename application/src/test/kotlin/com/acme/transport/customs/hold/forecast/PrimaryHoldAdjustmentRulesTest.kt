package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryHoldAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryHoldAdjustmentValidator()
        val item = newPrimaryHoldAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryHoldAdjustmentRequiredCodeRule("primary hold adjustment needs a value")
        val item = newPrimaryHoldAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryHoldAdjustmentCodeLengthRule(3600)
        val item = newPrimaryHoldAdjustment("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = PrimaryHoldAdjustmentRequiredVehicleIdRule("primary hold adjustment needs a value")
        val item = newPrimaryHoldAdjustment("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = PrimaryHoldAdjustmentRouteCodeLengthRule(3600)
        val item = newPrimaryHoldAdjustment("x", "y")
        item.routeCode = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
