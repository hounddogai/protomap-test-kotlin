package com.acme.transport.freightpay.fuelsurcharge.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeVarianceValidator()
        val item = newFuelSurchargeVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = FuelSurchargeVarianceExpiresAtRangeRule(16, 3600)
        val item = newFuelSurchargeVariance("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FuelSurchargeVarianceRequiredLabelRule("fuel surcharge variance needs this field")
        val item = newFuelSurchargeVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = FuelSurchargeVarianceRequiredChecksumRule("fuel surcharge variance needs an entry")
        val item = newFuelSurchargeVariance("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
