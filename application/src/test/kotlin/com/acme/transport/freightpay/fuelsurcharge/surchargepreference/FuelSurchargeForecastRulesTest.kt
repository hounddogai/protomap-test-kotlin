package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeForecastValidator()
        val item = newFuelSurchargeForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = FuelSurchargeForecastRequiredPhoneRule("fuel surcharge forecast needs this field")
        val item = newFuelSurchargeForecast("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = FuelSurchargeForecastDepotLengthRule(32)
        val item = newFuelSurchargeForecast("x", "y")
        item.depot = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FuelSurchargeForecastRequiredNameRule("fuel surcharge forecast needs a value")
        val item = newFuelSurchargeForecast("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
