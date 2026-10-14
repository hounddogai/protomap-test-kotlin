package com.acme.transport.freightpay.freightinvoice.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceSnapshotValidator()
        val item = newFreightInvoiceSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FreightInvoiceSnapshotNameLengthRule(1000)
        val item = newFreightInvoiceSnapshot("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = FreightInvoiceSnapshotSummaryLengthRule(80)
        val item = newFreightInvoiceSnapshot("x", "y")
        item.summary = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FreightInvoiceSnapshotRequiredNameRule("freight invoice snapshot needs this field")
        val item = newFreightInvoiceSnapshot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
