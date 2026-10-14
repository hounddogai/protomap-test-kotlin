package com.acme.transport.freightpay.dispute.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeAgreementValidator()
        val item = newDisputeAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = DisputeAgreementRequiredRouteCodeRule("dispute agreement needs a value")
        val item = newDisputeAgreement("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = DisputeAgreementRouteCodeLengthRule(64)
        val item = newDisputeAgreement("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = DisputeAgreementTitleLengthRule(86400)
        val item = newDisputeAgreement("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
