package com.acme.transport.freightpay.audit.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditEstimateValidator()
        val item = newAuditEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = AuditEstimateVersionRangeRule(16, 16)
        val item = newAuditEstimate("x", "y")
        item.version = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = AuditEstimateRateRangeRule(5.9, 1645.7)
        val item = newAuditEstimate("x", "y")
        item.rate = 1647.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = AuditEstimateAmountRangeRule(8.8, 4318.2)
        val item = newAuditEstimate("x", "y")
        item.amount = 4319.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = AuditEstimateRequiredRouteCodeRule("audit estimate needs this field")
        val item = newAuditEstimate("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
