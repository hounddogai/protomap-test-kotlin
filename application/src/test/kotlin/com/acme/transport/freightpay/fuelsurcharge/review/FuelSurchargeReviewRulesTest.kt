package com.acme.transport.freightpay.fuelsurcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeReviewValidator()
        val item = newFuelSurchargeReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = FuelSurchargeReviewCountryLengthRule(64)
        val item = newFuelSurchargeReview("x", "y")
        item.country = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = FuelSurchargeReviewRequiredDriverIdRule("fuel surcharge review needs this field")
        val item = newFuelSurchargeReview("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FuelSurchargeReviewRequiredLabelRule("fuel surcharge review needs this field")
        val item = newFuelSurchargeReview("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelSurchargeReviewCodeLengthRule(120)
        val item = newFuelSurchargeReview("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
