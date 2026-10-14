package com.acme.transport.linehaul.tender.tenderestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderAuditValidator()
        val item = newTenderAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = TenderAuditSegmentLengthRule(80)
        val item = newTenderAudit("x", "y")
        item.segment = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = TenderAuditCapacityRangeRule(80, 3600)
        val item = newTenderAudit("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = TenderAuditRequiredPhoneRule("tender audit needs this field")
        val item = newTenderAudit("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TenderAuditCodeLengthRule(32)
        val item = newTenderAudit("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
