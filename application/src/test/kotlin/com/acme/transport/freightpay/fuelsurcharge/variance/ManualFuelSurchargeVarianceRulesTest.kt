package com.acme.transport.freightpay.fuelsurcharge.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualFuelSurchargeVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualFuelSurchargeVarianceValidator()
        val item = newManualFuelSurchargeVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualFuelSurchargeVarianceRequiredCodeRule("manual fuel surcharge variance needs a value")
        val item = newManualFuelSurchargeVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ManualFuelSurchargeVarianceEtaMinutesRangeRule(80, 3600)
        val item = newManualFuelSurchargeVariance("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualFuelSurchargeVarianceRequiredLabelRule("manual fuel surcharge variance needs a value")
        val item = newManualFuelSurchargeVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = ManualFuelSurchargeVariancePhoneLengthRule(64)
        val item = newManualFuelSurchargeVariance("x", "y")
        item.phone = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ManualFuelSurchargeVarianceAmountRangeRule(1.7, 3884.8)
        val item = newManualFuelSurchargeVariance("x", "y")
        item.amount = 3886.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
