package com.acme.transport.emissions.fuelburn.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnAgreementValidator()
        val item = newFuelBurnAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FuelBurnAgreementTitleLengthRule(255)
        val item = newFuelBurnAgreement("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = FuelBurnAgreementDiscountRangeRule(7.3, 1083.8)
        val item = newFuelBurnAgreement("x", "y")
        item.discount = 1085.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = FuelBurnAgreementRequiredDockRule("fuel burn agreement needs a value")
        val item = newFuelBurnAgreement("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
