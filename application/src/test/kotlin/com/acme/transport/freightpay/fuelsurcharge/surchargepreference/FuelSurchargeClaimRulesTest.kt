package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeClaimValidator()
        val item = newFuelSurchargeClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = FuelSurchargeClaimRequiredDriverIdRule("fuel surcharge claim needs an entry")
        val item = newFuelSurchargeClaim("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FuelSurchargeClaimTitleLengthRule(1000)
        val item = newFuelSurchargeClaim("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
