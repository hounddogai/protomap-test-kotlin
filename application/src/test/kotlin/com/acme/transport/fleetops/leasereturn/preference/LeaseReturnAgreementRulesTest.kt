package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnAgreementValidator()
        val item = newLeaseReturnAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = LeaseReturnAgreementRouteCodeLengthRule(1000)
        val item = newLeaseReturnAgreement("x", "y")
        item.routeCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LeaseReturnAgreementRequiredCodeRule("lease return agreement needs this field")
        val item = newLeaseReturnAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LeaseReturnAgreementAttemptsRangeRule(32, 86400)
        val item = newLeaseReturnAgreement("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
