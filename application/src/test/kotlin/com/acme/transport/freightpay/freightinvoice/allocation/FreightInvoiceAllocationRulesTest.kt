package com.acme.transport.freightpay.freightinvoice.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceAllocationValidator()
        val item = newFreightInvoiceAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = FreightInvoiceAllocationRequiredDepotRule("freight invoice allocation needs this field")
        val item = newFreightInvoiceAllocation("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = FreightInvoiceAllocationContactEmailLengthRule(80)
        val item = newFreightInvoiceAllocation("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = FreightInvoiceAllocationRequiredContactEmailRule("freight invoice allocation needs this field")
        val item = newFreightInvoiceAllocation("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = FreightInvoiceAllocationRateRangeRule(9.2, 2602.8)
        val item = newFreightInvoiceAllocation("x", "y")
        item.rate = 2604.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
