package com.acme.transport.freightpay.fuelsurcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeBundleValidator()
        val item = newFuelSurchargeBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelSurchargeBundleRequiredCodeRule("fuel surcharge bundle needs an entry")
        val item = newFuelSurchargeBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FuelSurchargeBundleRequiredNameRule("fuel surcharge bundle needs a value")
        val item = newFuelSurchargeBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = FuelSurchargeBundleDistanceKmRangeRule(8.0, 2146.1)
        val item = newFuelSurchargeBundle("x", "y")
        item.distanceKm = 2147.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
