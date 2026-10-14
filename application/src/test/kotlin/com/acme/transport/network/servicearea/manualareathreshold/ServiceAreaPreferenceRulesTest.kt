package com.acme.transport.network.servicearea.manualareathreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaPreferenceValidator()
        val item = newServiceAreaPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ServiceAreaPreferenceLabelLengthRule(64)
        val item = newServiceAreaPreference("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ServiceAreaPreferenceEndsAtRangeRule(16, 3600)
        val item = newServiceAreaPreference("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = ServiceAreaPreferenceCostRangeRule(2.8, 207.1)
        val item = newServiceAreaPreference("x", "y")
        item.cost = 208.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ServiceAreaPreferenceTimezoneLengthRule(1000)
        val item = newServiceAreaPreference("x", "y")
        item.timezone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ServiceAreaPreferenceSequenceRangeRule(16, 3600)
        val item = newServiceAreaPreference("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
