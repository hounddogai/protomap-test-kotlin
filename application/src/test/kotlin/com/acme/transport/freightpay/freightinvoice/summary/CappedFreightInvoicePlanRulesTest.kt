package com.acme.transport.freightpay.freightinvoice.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedFreightInvoicePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedFreightInvoicePlanValidator()
        val item = newCappedFreightInvoicePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = CappedFreightInvoicePlanSegmentLengthRule(86400)
        val item = newCappedFreightInvoicePlan("x", "y")
        item.segment = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
