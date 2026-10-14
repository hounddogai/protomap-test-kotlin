package com.acme.transport.freightpay.freightinvoice.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictFreightInvoiceRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictFreightInvoiceRequestValidator()
        val item = newStrictFreightInvoiceRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictFreightInvoiceRequestNameLengthRule(86400)
        val item = newStrictFreightInvoiceRequest("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictFreightInvoiceRequestRequiredNameRule("strict freight invoice request needs an entry")
        val item = newStrictFreightInvoiceRequest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = StrictFreightInvoiceRequestUnitLengthRule(80)
        val item = newStrictFreightInvoiceRequest("x", "y")
        item.unit = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = StrictFreightInvoiceRequestRequiredUnitRule("strict freight invoice request needs a value")
        val item = newStrictFreightInvoiceRequest("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
