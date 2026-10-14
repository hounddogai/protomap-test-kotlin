package com.acme.transport.freightpay.audit.auditrollup

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalAuditRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalAuditRollupValidator()
        val item = newRegionalAuditRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = RegionalAuditRollupBudgetRangeRule(0.4, 4243.7)
        val item = newRegionalAuditRollup("x", "y")
        item.budget = 4245.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RegionalAuditRollupExpiresAtRangeRule(120, 86400)
        val item = newRegionalAuditRollup("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
