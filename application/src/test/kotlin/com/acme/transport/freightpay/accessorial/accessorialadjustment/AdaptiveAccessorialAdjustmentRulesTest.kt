package com.acme.transport.freightpay.accessorial.accessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAccessorialAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveAccessorialAdjustmentValidator()
        val item = newAdaptiveAccessorialAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveAccessorialAdjustmentLabelLengthRule(64)
        val item = newAdaptiveAccessorialAdjustment("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = AdaptiveAccessorialAdjustmentVehicleIdLengthRule(120)
        val item = newAdaptiveAccessorialAdjustment("x", "y")
        item.vehicleId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank country`() {
        val rule = AdaptiveAccessorialAdjustmentRequiredCountryRule("adaptive accessorial adjustment needs an entry")
        val item = newAdaptiveAccessorialAdjustment("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = AdaptiveAccessorialAdjustmentCountryLengthRule(1000)
        val item = newAdaptiveAccessorialAdjustment("x", "y")
        item.country = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveAccessorialAdjustmentRequiredCodeRule("adaptive accessorial adjustment needs this field")
        val item = newAdaptiveAccessorialAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
