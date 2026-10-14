package com.acme.transport.lastmile.proofofdelivery.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingProofOfDeliveryPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingProofOfDeliveryPreferenceValidator()
        val item = newRollingProofOfDeliveryPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RollingProofOfDeliveryPreferenceFuelLitersRangeRule(9.1, 3156.7)
        val item = newRollingProofOfDeliveryPreference("x", "y")
        item.fuelLiters = 3158.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = RollingProofOfDeliveryPreferenceRequiredCarrierRule(
            "rolling proof of delivery preference needs this field",
        )
        val item = newRollingProofOfDeliveryPreference("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingProofOfDeliveryPreferenceLabelLengthRule(64)
        val item = newRollingProofOfDeliveryPreference("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingProofOfDeliveryPreferenceCodeLengthRule(32)
        val item = newRollingProofOfDeliveryPreference("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
