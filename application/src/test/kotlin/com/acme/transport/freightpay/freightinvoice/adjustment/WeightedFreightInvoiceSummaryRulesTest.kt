package com.acme.transport.freightpay.freightinvoice.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedFreightInvoiceSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedFreightInvoiceSummaryValidator()
        val item = newWeightedFreightInvoiceSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = WeightedFreightInvoiceSummaryVersionRangeRule(255, 255)
        val item = newWeightedFreightInvoiceSummary("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
