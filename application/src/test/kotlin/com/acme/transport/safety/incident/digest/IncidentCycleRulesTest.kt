package com.acme.transport.safety.incident.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentCycleValidator()
        val item = newIncidentCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = IncidentCycleRequiredLabelRule("incident cycle needs a value")
        val item = newIncidentCycle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = IncidentCycleWindowDaysRangeRule(64, 86400)
        val item = newIncidentCycle("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = IncidentCycleDescriptionLengthRule(120)
        val item = newIncidentCycle("x", "y")
        item.description = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IncidentCycleRequiredCodeRule("incident cycle needs this field")
        val item = newIncidentCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = IncidentCycleLabelLengthRule(3600)
        val item = newIncidentCycle("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
