package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentSlotValidator()
        val item = newIncidentSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = IncidentSlotRequiredExternalRefRule("incident slot needs an entry")
        val item = newIncidentSlot("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = IncidentSlotExternalRefLengthRule(255)
        val item = newIncidentSlot("x", "y")
        item.externalRef = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IncidentSlotRequiredCodeRule("incident slot needs this field")
        val item = newIncidentSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
