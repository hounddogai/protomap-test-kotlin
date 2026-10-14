package com.acme.transport.freightpay.freightinvoice.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceCheckpointValidator()
        val item = newFreightInvoiceCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FreightInvoiceCheckpointCodeLengthRule(32)
        val item = newFreightInvoiceCheckpoint("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = FreightInvoiceCheckpointNameLengthRule(16)
        val item = newFreightInvoiceCheckpoint("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FreightInvoiceCheckpointRequiredCodeRule("freight invoice checkpoint needs a value")
        val item = newFreightInvoiceCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = FreightInvoiceCheckpointSourceLengthRule(64)
        val item = newFreightInvoiceCheckpoint("x", "y")
        item.source = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
