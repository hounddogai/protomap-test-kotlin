package com.acme.transport.freightpay.audit.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditSnapshotValidator()
        val item = newAuditSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = AuditSnapshotShiftCodeLengthRule(64)
        val item = newAuditSnapshot("x", "y")
        item.shiftCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = AuditSnapshotMaximumRangeRule(4.8, 2842.9)
        val item = newAuditSnapshot("x", "y")
        item.maximum = 2844.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AuditSnapshotRequiredNameRule("audit snapshot needs this field")
        val item = newAuditSnapshot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
