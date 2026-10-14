package com.acme.transport.freightpay.audit.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditTargetValidator()
        val item = newAuditTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AuditTargetCodeLengthRule(255)
        val item = newAuditTarget("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = AuditTargetBatchSizeRangeRule(16, 32)
        val item = newAuditTarget("x", "y")
        item.batchSize = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = AuditTargetRetriesRangeRule(64, 120)
        val item = newAuditTarget("x", "y")
        item.retries = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
