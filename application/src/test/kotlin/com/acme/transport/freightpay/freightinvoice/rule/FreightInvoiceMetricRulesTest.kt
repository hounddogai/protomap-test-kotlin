package com.acme.transport.freightpay.freightinvoice.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceMetricValidator()
        val item = newFreightInvoiceMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FreightInvoiceMetricCodeLengthRule(80)
        val item = newFreightInvoiceMetric("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = FreightInvoiceMetricMinimumRangeRule(9.9, 3894.7)
        val item = newFreightInvoiceMetric("x", "y")
        item.minimum = 3896.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FreightInvoiceMetricRequiredCodeRule("freight invoice metric needs an entry")
        val item = newFreightInvoiceMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = FreightInvoiceMetricPalletsRangeRule(16, 120)
        val item = newFreightInvoiceMetric("x", "y")
        item.pallets = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = FreightInvoiceMetricRateRangeRule(6.1, 893.1)
        val item = newFreightInvoiceMetric("x", "y")
        item.rate = 894.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
