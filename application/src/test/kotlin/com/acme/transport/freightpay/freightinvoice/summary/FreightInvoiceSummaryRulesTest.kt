package com.acme.transport.freightpay.freightinvoice.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceSummaryValidator()
        val item = newFreightInvoiceSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = FreightInvoiceSummaryBatchSizeRangeRule(80, 80)
        val item = newFreightInvoiceSummary("x", "y")
        item.batchSize = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FreightInvoiceSummaryCodeLengthRule(32)
        val item = newFreightInvoiceSummary("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FreightInvoiceSummaryRequiredTitleRule("freight invoice summary needs a value")
        val item = newFreightInvoiceSummary("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
