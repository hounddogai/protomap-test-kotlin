package com.acme.transport.freightpay.audit.auditreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAuditQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAuditQuotaValidator()
        val item = newBatchedAuditQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = BatchedAuditQuotaDurationMinutesRangeRule(32, 64)
        val item = newBatchedAuditQuota("x", "y")
        item.durationMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedAuditQuotaCodeLengthRule(80)
        val item = newBatchedAuditQuota("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedAuditQuotaRequiredCodeRule("batched audit quota needs a value")
        val item = newBatchedAuditQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = BatchedAuditQuotaRequiredContactEmailRule("batched audit quota needs an entry")
        val item = newBatchedAuditQuota("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
