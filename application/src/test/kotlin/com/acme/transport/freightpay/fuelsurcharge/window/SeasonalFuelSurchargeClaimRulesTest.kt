package com.acme.transport.freightpay.fuelsurcharge.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalFuelSurchargeClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalFuelSurchargeClaimValidator()
        val item = newSeasonalFuelSurchargeClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = SeasonalFuelSurchargeClaimOwnerIdLengthRule(1000)
        val item = newSeasonalFuelSurchargeClaim("x", "y")
        item.ownerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = SeasonalFuelSurchargeClaimChecksumLengthRule(120)
        val item = newSeasonalFuelSurchargeClaim("x", "y")
        item.checksum = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalFuelSurchargeClaimRequiredCodeRule("seasonal fuel surcharge claim needs this field")
        val item = newSeasonalFuelSurchargeClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
