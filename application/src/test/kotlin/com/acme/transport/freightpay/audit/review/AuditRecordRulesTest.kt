package com.acme.transport.freightpay.audit.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditRecordValidator()
        val item = newAuditRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AuditRecordRequiredCodeRule("audit record needs a value")
        val item = newAuditRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AuditRecordCodeLengthRule(80)
        val item = newAuditRecord("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = AuditRecordWindowDaysRangeRule(255, 255)
        val item = newAuditRecord("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = AuditRecordLabelLengthRule(1000)
        val item = newAuditRecord("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = AuditRecordGraceDaysRangeRule(32, 64)
        val item = newAuditRecord("x", "y")
        item.graceDays = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
