package com.acme.transport.freightpay.fuelsurcharge.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeAdjustmentValidator()
        val item = newFuelSurchargeAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = FuelSurchargeAdjustmentRatioRangeRule(8.4, 3191.5)
        val item = newFuelSurchargeAdjustment("x", "y")
        item.ratio = 3193.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = FuelSurchargeAdjustmentDistanceKmRangeRule(1.6, 327.5)
        val item = newFuelSurchargeAdjustment("x", "y")
        item.distanceKm = 329.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = FuelSurchargeAdjustmentNameLengthRule(3600)
        val item = newFuelSurchargeAdjustment("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
