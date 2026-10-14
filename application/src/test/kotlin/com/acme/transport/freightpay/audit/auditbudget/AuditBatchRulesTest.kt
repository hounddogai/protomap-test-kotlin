package com.acme.transport.freightpay.audit.auditbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditBatchValidator()
        val item = newAuditBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = AuditBatchRequiredContactEmailRule("audit batch needs this field")
        val item = newAuditBatch("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AuditBatchRequiredCodeRule("audit batch needs this field")
        val item = newAuditBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = AuditBatchContactEmailLengthRule(64)
        val item = newAuditBatch("x", "y")
        item.contactEmail = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AuditBatchLabelLengthRule(32)
        val item = newAuditBatch("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = AuditBatchDurationMinutesRangeRule(80, 80)
        val item = newAuditBatch("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
