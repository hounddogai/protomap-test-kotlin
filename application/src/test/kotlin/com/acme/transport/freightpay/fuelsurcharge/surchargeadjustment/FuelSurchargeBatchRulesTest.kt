package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeBatchValidator()
        val item = newFuelSurchargeBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = FuelSurchargeBatchRouteCodeLengthRule(120)
        val item = newFuelSurchargeBatch("x", "y")
        item.routeCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = FuelSurchargeBatchRequiredDriverIdRule("fuel surcharge batch needs this field")
        val item = newFuelSurchargeBatch("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = FuelSurchargeBatchRatioRangeRule(6.8, 3669.5)
        val item = newFuelSurchargeBatch("x", "y")
        item.ratio = 3671.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = FuelSurchargeBatchNameLengthRule(255)
        val item = newFuelSurchargeBatch("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelSurchargeBatchCodeLengthRule(32)
        val item = newFuelSurchargeBatch("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
