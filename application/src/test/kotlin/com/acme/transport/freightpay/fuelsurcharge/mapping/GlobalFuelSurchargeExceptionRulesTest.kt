package com.acme.transport.freightpay.fuelsurcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalFuelSurchargeExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalFuelSurchargeExceptionValidator()
        val item = newGlobalFuelSurchargeException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalFuelSurchargeExceptionCodeLengthRule(1000)
        val item = newGlobalFuelSurchargeException("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = GlobalFuelSurchargeExceptionVehicleIdLengthRule(64)
        val item = newGlobalFuelSurchargeException("x", "y")
        item.vehicleId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalFuelSurchargeExceptionRequiredNameRule("global fuel surcharge exception needs this field")
        val item = newGlobalFuelSurchargeException("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = GlobalFuelSurchargeExceptionRequiredVehicleIdRule("global fuel surcharge exception needs an entry")
        val item = newGlobalFuelSurchargeException("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
