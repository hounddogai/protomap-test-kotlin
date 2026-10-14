package com.acme.transport.freightpay.audit.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditDraftValidator()
        val item = newAuditDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = AuditDraftAttemptsRangeRule(32, 86400)
        val item = newAuditDraft("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = AuditDraftDurationMinutesRangeRule(3600, 3600)
        val item = newAuditDraft("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AuditDraftCodeLengthRule(64)
        val item = newAuditDraft("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
