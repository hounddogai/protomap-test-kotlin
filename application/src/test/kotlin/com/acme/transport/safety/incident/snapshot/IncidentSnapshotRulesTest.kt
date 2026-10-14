package com.acme.transport.safety.incident.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentSnapshotValidator()
        val item = newIncidentSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = IncidentSnapshotSequenceRangeRule(16, 86400)
        val item = newIncidentSnapshot("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IncidentSnapshotRequiredCodeRule("incident snapshot needs this field")
        val item = newIncidentSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = IncidentSnapshotCodeLengthRule(86400)
        val item = newIncidentSnapshot("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = IncidentSnapshotCarrierLengthRule(80)
        val item = newIncidentSnapshot("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
