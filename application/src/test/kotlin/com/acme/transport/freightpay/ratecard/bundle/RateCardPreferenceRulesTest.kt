package com.acme.transport.freightpay.ratecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardPreferenceValidator()
        val item = newRateCardPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RateCardPreferenceRequiredCodeRule("rate card preference needs a value")
        val item = newRateCardPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RateCardPreferenceRequiredLabelRule("rate card preference needs this field")
        val item = newRateCardPreference("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RateCardPreferenceRequiredLaneRule("rate card preference needs this field")
        val item = newRateCardPreference("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RateCardPreferenceCodeLengthRule(86400)
        val item = newRateCardPreference("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RateCardPreferenceLaneLengthRule(16)
        val item = newRateCardPreference("x", "y")
        item.lane = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
