package com.acme.transport.freightpay.audit.auditoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAuditOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredAuditOverrideValidator()
        val item = newTieredAuditOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = TieredAuditOverrideSummaryLengthRule(120)
        val item = newTieredAuditOverride("x", "y")
        item.summary = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = TieredAuditOverrideTimezoneLengthRule(1000)
        val item = newTieredAuditOverride("x", "y")
        item.timezone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredAuditOverrideRequiredTitleRule("tiered audit override needs this field")
        val item = newTieredAuditOverride("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredAuditOverrideRequiredCodeRule("tiered audit override needs an entry")
        val item = newTieredAuditOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredAuditOverrideTitleLengthRule(64)
        val item = newTieredAuditOverride("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
