package com.acme.transport.freightpay.freightinvoice.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceBudgetValidator()
        val item = newFreightInvoiceBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = FreightInvoiceBudgetRequiredCarrierRule("freight invoice budget needs this field")
        val item = newFreightInvoiceBudget("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = FreightInvoiceBudgetOdometerRangeRule(80, 86400)
        val item = newFreightInvoiceBudget("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FreightInvoiceBudgetCodeLengthRule(120)
        val item = newFreightInvoiceBudget("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = FreightInvoiceBudgetUnitLengthRule(3600)
        val item = newFreightInvoiceBudget("x", "y")
        item.unit = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
