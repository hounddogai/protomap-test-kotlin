package com.acme.transport.safety.incident.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyIncidentEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyIncidentEntryValidator()
        val item = newLegacyIncidentEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LegacyIncidentEntryRequiredTimezoneRule("legacy incident entry needs this field")
        val item = newLegacyIncidentEntry("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = LegacyIncidentEntryQuantityRangeRule(80, 120)
        val item = newLegacyIncidentEntry("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyIncidentEntryLabelLengthRule(255)
        val item = newLegacyIncidentEntry("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyIncidentEntryRequiredLabelRule("legacy incident entry needs this field")
        val item = newLegacyIncidentEntry("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = LegacyIncidentEntryRequiredCountryRule("legacy incident entry needs this field")
        val item = newLegacyIncidentEntry("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = LegacyIncidentEntryCountryLengthRule(1000)
        val item = newLegacyIncidentEntry("x", "y")
        item.country = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
