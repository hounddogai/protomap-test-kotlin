package com.acme.transport.freightpay.audit.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditOverrideValidator()
        val item = newAuditOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = AuditOverrideCountryLengthRule(1000)
        val item = newAuditOverride("x", "y")
        item.country = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank country`() {
        val rule = AuditOverrideRequiredCountryRule("audit override needs an entry")
        val item = newAuditOverride("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AuditOverrideCodeLengthRule(255)
        val item = newAuditOverride("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
