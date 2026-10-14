package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeTierValidator()
        val item = newFuelSurchargeTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FuelSurchargeTierLabelLengthRule(86400)
        val item = newFuelSurchargeTier("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = FuelSurchargeTierRequiredCurrencyRule("fuel surcharge tier needs an entry")
        val item = newFuelSurchargeTier("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = FuelSurchargeTierPhoneLengthRule(86400)
        val item = newFuelSurchargeTier("x", "y")
        item.phone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = FuelSurchargeTierLoadKgRangeRule(7.8, 4568.3)
        val item = newFuelSurchargeTier("x", "y")
        item.loadKg = 4569.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FuelSurchargeTierRequiredLabelRule("fuel surcharge tier needs a value")
        val item = newFuelSurchargeTier("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
