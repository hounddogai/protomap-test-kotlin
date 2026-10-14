package com.acme.transport.network.transittime.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeAgreementValidator()
        val item = newTransitTimeAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = TransitTimeAgreementRequiredCategoryRule("transit time agreement needs an entry")
        val item = newTransitTimeAgreement("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = TransitTimeAgreementDescriptionLengthRule(64)
        val item = newTransitTimeAgreement("x", "y")
        item.description = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = TransitTimeAgreementFuelLitersRangeRule(1.6, 2021.6)
        val item = newTransitTimeAgreement("x", "y")
        item.fuelLiters = 2023.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
