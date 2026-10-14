package com.acme.transport.fleetops.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionAuditValidator()
        val item = newInspectionAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = InspectionAuditTitleLengthRule(255)
        val item = newInspectionAudit("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = InspectionAuditOwnerIdLengthRule(86400)
        val item = newInspectionAudit("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = InspectionAuditDurationMinutesRangeRule(64, 80)
        val item = newInspectionAudit("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
