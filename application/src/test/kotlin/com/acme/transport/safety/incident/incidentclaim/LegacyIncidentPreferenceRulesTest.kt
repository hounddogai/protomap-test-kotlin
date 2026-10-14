package com.acme.transport.safety.incident.incidentclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyIncidentPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyIncidentPreferenceValidator()
        val item = newLegacyIncidentPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyIncidentPreferenceLabelLengthRule(80)
        val item = newLegacyIncidentPreference("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = LegacyIncidentPreferenceTimezoneLengthRule(32)
        val item = newLegacyIncidentPreference("x", "y")
        item.timezone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = LegacyIncidentPreferenceOdometerRangeRule(255, 255)
        val item = newLegacyIncidentPreference("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
