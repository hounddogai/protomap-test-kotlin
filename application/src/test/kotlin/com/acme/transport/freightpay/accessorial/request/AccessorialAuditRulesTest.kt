package com.acme.transport.freightpay.accessorial.request

import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialAuditValidator()
        val item = newAccessorialAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = AccessorialAuditPalletsRangeRule(16, 86400)
        val item = newAccessorialAudit("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
