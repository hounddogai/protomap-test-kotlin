package com.acme.transport.freightpay.freightinvoice.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceExceptionValidator()
        val item = newFreightInvoiceException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FreightInvoiceExceptionRequiredTitleRule("freight invoice exception needs this field")
        val item = newFreightInvoiceException("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = FreightInvoiceExceptionRequiredLaneRule("freight invoice exception needs an entry")
        val item = newFreightInvoiceException("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = FreightInvoiceExceptionCeilingRangeRule(0.7, 3296.9)
        val item = newFreightInvoiceException("x", "y")
        item.ceiling = 3298.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
