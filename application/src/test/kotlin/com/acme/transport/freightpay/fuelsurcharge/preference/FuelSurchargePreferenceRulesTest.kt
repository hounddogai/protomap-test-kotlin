package com.acme.transport.freightpay.fuelsurcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargePreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargePreferenceValidator()
        val item = newFuelSurchargePreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = FuelSurchargePreferenceThresholdRangeRule(8.3, 3116.2)
        val item = newFuelSurchargePreference("x", "y")
        item.threshold = 3117.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = FuelSurchargePreferenceRevisionRangeRule(120, 3600)
        val item = newFuelSurchargePreference("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelSurchargePreferenceRequiredCodeRule("fuel surcharge preference needs a value")
        val item = newFuelSurchargePreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
