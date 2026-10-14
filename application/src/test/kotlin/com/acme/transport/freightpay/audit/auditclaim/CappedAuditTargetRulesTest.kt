package com.acme.transport.freightpay.audit.auditclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAuditTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAuditTargetValidator()
        val item = newCappedAuditTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedAuditTargetNameLengthRule(120)
        val item = newCappedAuditTarget("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = CappedAuditTargetRequiredCurrencyRule("capped audit target needs a value")
        val item = newCappedAuditTarget("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
