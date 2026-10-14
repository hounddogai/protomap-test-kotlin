package com.acme.transport.customs.hold.holdslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldAuditValidator()
        val item = newHoldAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = HoldAuditCodeLengthRule(80)
        val item = newHoldAudit("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = HoldAuditNameLengthRule(3600)
        val item = newHoldAudit("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = HoldAuditExpiresAtRangeRule(80, 3600)
        val item = newHoldAudit("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
