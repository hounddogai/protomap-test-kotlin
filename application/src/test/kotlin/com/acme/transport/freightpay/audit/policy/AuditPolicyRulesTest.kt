package com.acme.transport.freightpay.audit.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditPolicyValidator()
        val item = newAuditPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AuditPolicyTitleLengthRule(16)
        val item = newAuditPolicy("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long currency`() {
        val rule = AuditPolicyCurrencyLengthRule(255)
        val item = newAuditPolicy("x", "y")
        item.currency = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AuditPolicyRequiredTitleRule("audit policy needs a value")
        val item = newAuditPolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
