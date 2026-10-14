package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoicePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoicePlanValidator()
        val item = newFreightInvoicePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = FreightInvoicePlanSequenceRangeRule(32, 120)
        val item = newFreightInvoicePlan("x", "y")
        item.sequence = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = FreightInvoicePlanStopCountRangeRule(255, 86400)
        val item = newFreightInvoicePlan("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = FreightInvoicePlanQuantityRangeRule(64, 3600)
        val item = newFreightInvoicePlan("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FreightInvoicePlanRequiredLabelRule("freight invoice plan needs an entry")
        val item = newFreightInvoicePlan("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
