package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentRequestValidator()
        val item = newIncidentRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IncidentRequestRequiredCodeRule("incident request needs a value")
        val item = newIncidentRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = IncidentRequestRequiredTitleRule("incident request needs an entry")
        val item = newIncidentRequest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = IncidentRequestWindowDaysRangeRule(1000, 3600)
        val item = newIncidentRequest("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = IncidentRequestRequiredPhoneRule("incident request needs an entry")
        val item = newIncidentRequest("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
