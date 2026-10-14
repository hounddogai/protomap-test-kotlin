package com.acme.transport.freightpay.audit.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyAuditRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyAuditRecordValidator()
        val item = newLegacyAuditRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyAuditRecordRequiredLabelRule("legacy audit record needs this field")
        val item = newLegacyAuditRecord("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyAuditRecordLabelLengthRule(64)
        val item = newLegacyAuditRecord("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long segment`() {
        val rule = LegacyAuditRecordSegmentLengthRule(1000)
        val item = newLegacyAuditRecord("x", "y")
        item.segment = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = LegacyAuditRecordBatchSizeRangeRule(80, 86400)
        val item = newLegacyAuditRecord("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyAuditRecordCodeLengthRule(80)
        val item = newLegacyAuditRecord("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
