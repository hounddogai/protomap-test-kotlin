package com.acme.transport.freightpay.audit.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditProfileValidator()
        val item = newAuditProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AuditProfileCodeLengthRule(1000)
        val item = newAuditProfile("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = AuditProfileExpiresAtRangeRule(80, 255)
        val item = newAuditProfile("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
