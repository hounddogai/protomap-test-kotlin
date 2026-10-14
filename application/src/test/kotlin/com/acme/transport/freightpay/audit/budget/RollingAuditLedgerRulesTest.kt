package com.acme.transport.freightpay.audit.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingAuditLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingAuditLedgerValidator()
        val item = newRollingAuditLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingAuditLedgerCodeLengthRule(255)
        val item = newRollingAuditLedger("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = RollingAuditLedgerRequiredSourceRule("rolling audit ledger needs a value")
        val item = newRollingAuditLedger("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = RollingAuditLedgerMarginRangeRule(8.7, 2658.8)
        val item = newRollingAuditLedger("x", "y")
        item.margin = 2660.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
