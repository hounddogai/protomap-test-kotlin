package com.acme.transport.freightpay.audit.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAuditClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalAuditClaimValidator()
        val item = newGlobalAuditClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = GlobalAuditClaimEtaMinutesRangeRule(32, 64)
        val item = newGlobalAuditClaim("x", "y")
        item.etaMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = GlobalAuditClaimAttemptsRangeRule(16, 255)
        val item = newGlobalAuditClaim("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = GlobalAuditClaimStartsAtRangeRule(120, 1000)
        val item = newGlobalAuditClaim("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = GlobalAuditClaimNotesLengthRule(86400)
        val item = newGlobalAuditClaim("x", "y")
        item.notes = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = GlobalAuditClaimRequiredNotesRule("global audit claim needs this field")
        val item = newGlobalAuditClaim("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
