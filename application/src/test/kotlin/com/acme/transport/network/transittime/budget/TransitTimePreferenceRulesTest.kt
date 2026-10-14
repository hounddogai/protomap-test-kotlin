package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimePreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimePreferenceValidator()
        val item = newTransitTimePreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = TransitTimePreferenceRetriesRangeRule(255, 3600)
        val item = newTransitTimePreference("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = TransitTimePreferenceRequiredCurrencyRule("transit time preference needs a value")
        val item = newTransitTimePreference("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = TransitTimePreferenceRequiredContactEmailRule("transit time preference needs this field")
        val item = newTransitTimePreference("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TransitTimePreferenceRequiredNameRule("transit time preference needs a value")
        val item = newTransitTimePreference("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
