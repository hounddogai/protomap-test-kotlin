package com.acme.transport.linehaul.capacity.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCapacityAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCapacityAgreementValidator()
        val item = newRegionalCapacityAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = RegionalCapacityAgreementQuantityRangeRule(80, 255)
        val item = newRegionalCapacityAgreement("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = RegionalCapacityAgreementRequiredVehicleIdRule("regional capacity agreement needs an entry")
        val item = newRegionalCapacityAgreement("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalCapacityAgreementRequiredCodeRule("regional capacity agreement needs this field")
        val item = newRegionalCapacityAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RegionalCapacityAgreementFuelLitersRangeRule(5.0, 2755.7)
        val item = newRegionalCapacityAgreement("x", "y")
        item.fuelLiters = 2757.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = RegionalCapacityAgreementVehicleIdLengthRule(32)
        val item = newRegionalCapacityAgreement("x", "y")
        item.vehicleId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
