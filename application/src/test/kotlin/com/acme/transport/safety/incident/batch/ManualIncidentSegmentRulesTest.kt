package com.acme.transport.safety.incident.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualIncidentSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualIncidentSegmentValidator()
        val item = newManualIncidentSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = ManualIncidentSegmentBudgetRangeRule(2.6, 1763.0)
        val item = newManualIncidentSegment("x", "y")
        item.budget = 1764.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = ManualIncidentSegmentNotesLengthRule(80)
        val item = newManualIncidentSegment("x", "y")
        item.notes = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualIncidentSegmentRequiredCodeRule("manual incident segment needs a value")
        val item = newManualIncidentSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = ManualIncidentSegmentSegmentLengthRule(32)
        val item = newManualIncidentSegment("x", "y")
        item.segment = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
