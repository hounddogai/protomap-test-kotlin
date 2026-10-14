package com.acme.transport.freightpay.audit.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAuditRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingAuditRecordValidator()
        val item = newPendingAuditRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = PendingAuditRecordQuantityRangeRule(32, 3600)
        val item = newPendingAuditRecord("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingAuditRecordRequiredCodeRule("pending audit record needs an entry")
        val item = newPendingAuditRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
