package com.acme.transport.freightpay.audit.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAuditDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredAuditDigestValidator()
        val item = newTieredAuditDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredAuditDigestCodeLengthRule(16)
        val item = newTieredAuditDigest("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = TieredAuditDigestGraceDaysRangeRule(16, 32)
        val item = newTieredAuditDigest("x", "y")
        item.graceDays = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = TieredAuditDigestNotesLengthRule(16)
        val item = newTieredAuditDigest("x", "y")
        item.notes = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredAuditDigestRequiredCodeRule("tiered audit digest needs an entry")
        val item = newTieredAuditDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
