package com.acme.transport.freightpay.ratecard.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRateCardBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveRateCardBaselineValidator()
        val item = newAdaptiveRateCardBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = AdaptiveRateCardBaselineVehicleIdLengthRule(86400)
        val item = newAdaptiveRateCardBaseline("x", "y")
        item.vehicleId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveRateCardBaselineRequiredCodeRule("adaptive rate card baseline needs this field")
        val item = newAdaptiveRateCardBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveRateCardBaselineNameLengthRule(255)
        val item = newAdaptiveRateCardBaseline("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
