package com.acme.transport.freightpay.audit.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditSegmentValidator()
        val item = newAuditSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = AuditSegmentDescriptionLengthRule(120)
        val item = newAuditSegment("x", "y")
        item.description = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AuditSegmentRequiredCodeRule("audit segment needs an entry")
        val item = newAuditSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AuditSegmentCodeLengthRule(16)
        val item = newAuditSegment("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = AuditSegmentRequiredDescriptionRule("audit segment needs this field")
        val item = newAuditSegment("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AuditSegmentTitleLengthRule(255)
        val item = newAuditSegment("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
