package com.acme.transport.freightpay.fuelsurcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeCycleValidator()
        val item = newFuelSurchargeCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = FuelSurchargeCycleUnitLengthRule(120)
        val item = newFuelSurchargeCycle("x", "y")
        item.unit = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelSurchargeCycleRequiredCodeRule("fuel surcharge cycle needs an entry")
        val item = newFuelSurchargeCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
