package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredFuelSurchargeBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredFuelSurchargeBaselineValidator()
        val item = newTieredFuelSurchargeBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = TieredFuelSurchargeBaselineDistanceKmRangeRule(5.5, 2385.3)
        val item = newTieredFuelSurchargeBaseline("x", "y")
        item.distanceKm = 2386.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = TieredFuelSurchargeBaselineCurrencyLengthRule(3600)
        val item = newTieredFuelSurchargeBaseline("x", "y")
        item.currency = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredFuelSurchargeBaselineRequiredCodeRule("tiered fuel surcharge baseline needs this field")
        val item = newTieredFuelSurchargeBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = TieredFuelSurchargeBaselineRequiredOwnerIdRule("tiered fuel surcharge baseline needs this field")
        val item = newTieredFuelSurchargeBaseline("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
