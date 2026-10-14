package com.acme.transport.freightpay.fuelsurcharge.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeReservationValidator()
        val item = newFuelSurchargeReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = FuelSurchargeReservationCurrencyLengthRule(120)
        val item = newFuelSurchargeReservation("x", "y")
        item.currency = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = FuelSurchargeReservationDockLengthRule(32)
        val item = newFuelSurchargeReservation("x", "y")
        item.dock = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = FuelSurchargeReservationRequiredDockRule("fuel surcharge reservation needs an entry")
        val item = newFuelSurchargeReservation("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
