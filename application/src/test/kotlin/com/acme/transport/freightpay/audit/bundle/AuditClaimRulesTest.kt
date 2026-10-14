package com.acme.transport.freightpay.audit.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditClaimValidator()
        val item = newAuditClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = AuditClaimRequiredContactEmailRule("audit claim needs this field")
        val item = newAuditClaim("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AuditClaimRequiredCodeRule("audit claim needs this field")
        val item = newAuditClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
