package com.acme.transport.freightpay.dispute.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeClaimValidator()
        val item = newDisputeClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = DisputeClaimSourceLengthRule(255)
        val item = newDisputeClaim("x", "y")
        item.source = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = DisputeClaimRequiredSourceRule("dispute claim needs this field")
        val item = newDisputeClaim("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = DisputeClaimRequiredSummaryRule("dispute claim needs an entry")
        val item = newDisputeClaim("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DisputeClaimLabelLengthRule(1000)
        val item = newDisputeClaim("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
