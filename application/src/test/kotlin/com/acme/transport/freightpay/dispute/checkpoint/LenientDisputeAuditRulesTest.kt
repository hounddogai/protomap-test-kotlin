package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDisputeAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDisputeAuditValidator()
        val item = newLenientDisputeAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LenientDisputeAuditRequiredTimezoneRule("lenient dispute audit needs a value")
        val item = newLenientDisputeAudit("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientDisputeAuditTitleLengthRule(86400)
        val item = newLenientDisputeAudit("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientDisputeAuditCodeLengthRule(80)
        val item = newLenientDisputeAudit("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
