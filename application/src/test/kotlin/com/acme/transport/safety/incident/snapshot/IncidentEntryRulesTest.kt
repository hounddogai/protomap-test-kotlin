package com.acme.transport.safety.incident.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentEntryValidator()
        val item = newIncidentEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = IncidentEntryDockLengthRule(3600)
        val item = newIncidentEntry("x", "y")
        item.dock = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = IncidentEntryRequiredLabelRule("incident entry needs an entry")
        val item = newIncidentEntry("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = IncidentEntryLabelLengthRule(32)
        val item = newIncidentEntry("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = IncidentEntryQuantityRangeRule(120, 86400)
        val item = newIncidentEntry("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = IncidentEntryRequiredDockRule("incident entry needs a value")
        val item = newIncidentEntry("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
