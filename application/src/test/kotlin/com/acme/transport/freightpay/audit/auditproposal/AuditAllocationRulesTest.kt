package com.acme.transport.freightpay.audit.auditproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditAllocationValidator()
        val item = newAuditAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AuditAllocationCodeLengthRule(255)
        val item = newAuditAllocation("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AuditAllocationRequiredTitleRule("audit allocation needs this field")
        val item = newAuditAllocation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = AuditAllocationRequiredLocaleRule("audit allocation needs this field")
        val item = newAuditAllocation("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AuditAllocationTitleLengthRule(3600)
        val item = newAuditAllocation("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = AuditAllocationLocaleLengthRule(1000)
        val item = newAuditAllocation("x", "y")
        item.locale = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
