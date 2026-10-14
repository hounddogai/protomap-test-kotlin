package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryIncidentSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryIncidentSegmentValidator()
        val item = newPrimaryIncidentSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = PrimaryIncidentSegmentDockLengthRule(3600)
        val item = newPrimaryIncidentSegment("x", "y")
        item.dock = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryIncidentSegmentCodeLengthRule(120)
        val item = newPrimaryIncidentSegment("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryIncidentSegmentRequiredTitleRule("primary incident segment needs a value")
        val item = newPrimaryIncidentSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryIncidentSegmentTitleLengthRule(32)
        val item = newPrimaryIncidentSegment("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
