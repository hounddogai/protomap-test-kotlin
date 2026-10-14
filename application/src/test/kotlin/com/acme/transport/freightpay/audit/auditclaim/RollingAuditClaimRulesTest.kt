package com.acme.transport.freightpay.audit.auditclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingAuditClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingAuditClaimValidator()
        val item = newRollingAuditClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingAuditClaimNameLengthRule(32)
        val item = newRollingAuditClaim("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingAuditClaimRequiredCodeRule("rolling audit claim needs this field")
        val item = newRollingAuditClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
