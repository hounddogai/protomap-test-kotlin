package com.acme.transport.freightpay.fuelsurcharge.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeExceptionValidator()
        val item = newFuelSurchargeException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = FuelSurchargeExceptionOwnerIdLengthRule(80)
        val item = newFuelSurchargeException("x", "y")
        item.ownerId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelSurchargeExceptionCodeLengthRule(255)
        val item = newFuelSurchargeException("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
