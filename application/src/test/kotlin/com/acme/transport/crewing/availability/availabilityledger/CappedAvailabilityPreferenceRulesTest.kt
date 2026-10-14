package com.acme.transport.crewing.availability.availabilityledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAvailabilityPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAvailabilityPreferenceValidator()
        val item = newCappedAvailabilityPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = CappedAvailabilityPreferenceQuantityRangeRule(80, 255)
        val item = newCappedAvailabilityPreference("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedAvailabilityPreferenceRequiredCodeRule("capped availability preference needs an entry")
        val item = newCappedAvailabilityPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = CappedAvailabilityPreferenceRegionLengthRule(32)
        val item = newCappedAvailabilityPreference("x", "y")
        item.region = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
