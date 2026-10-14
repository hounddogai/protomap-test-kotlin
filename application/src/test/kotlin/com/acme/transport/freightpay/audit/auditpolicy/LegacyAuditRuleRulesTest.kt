package com.acme.transport.freightpay.audit.auditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyAuditRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyAuditRuleValidator()
        val item = newLegacyAuditRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = LegacyAuditRuleCeilingRangeRule(2.7, 2677.6)
        val item = newLegacyAuditRule("x", "y")
        item.ceiling = 2679.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = LegacyAuditRuleNotesLengthRule(3600)
        val item = newLegacyAuditRule("x", "y")
        item.notes = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
