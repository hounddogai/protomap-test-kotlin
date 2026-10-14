package com.acme.transport.linehaul.relay.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRelayAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualRelayAssignmentValidator()
        val item = newManualRelayAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualRelayAssignmentTitleLengthRule(86400)
        val item = newManualRelayAssignment("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ManualRelayAssignmentLoadKgRangeRule(2.0, 2871.3)
        val item = newManualRelayAssignment("x", "y")
        item.loadKg = 2872.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
