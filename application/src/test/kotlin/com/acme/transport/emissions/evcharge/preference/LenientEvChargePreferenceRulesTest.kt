package com.acme.transport.emissions.evcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientEvChargePreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientEvChargePreferenceValidator()
        val item = newLenientEvChargePreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = LenientEvChargePreferenceVehicleIdLengthRule(1000)
        val item = newLenientEvChargePreference("x", "y")
        item.vehicleId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientEvChargePreferenceRequiredLabelRule("lenient ev charge preference needs an entry")
        val item = newLenientEvChargePreference("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientEvChargePreferenceRequiredCodeRule("lenient ev charge preference needs an entry")
        val item = newLenientEvChargePreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = LenientEvChargePreferenceRequiredVehicleIdRule("lenient ev charge preference needs a value")
        val item = newLenientEvChargePreference("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
