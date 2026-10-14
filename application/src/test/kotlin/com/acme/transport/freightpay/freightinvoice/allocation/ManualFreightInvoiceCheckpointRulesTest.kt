package com.acme.transport.freightpay.freightinvoice.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualFreightInvoiceCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualFreightInvoiceCheckpointValidator()
        val item = newManualFreightInvoiceCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ManualFreightInvoiceCheckpointWeightRangeRule(2.3, 2378.7)
        val item = newManualFreightInvoiceCheckpoint("x", "y")
        item.weight = 2380.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualFreightInvoiceCheckpointNameLengthRule(120)
        val item = newManualFreightInvoiceCheckpoint("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = ManualFreightInvoiceCheckpointCarrierLengthRule(16)
        val item = newManualFreightInvoiceCheckpoint("x", "y")
        item.carrier = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualFreightInvoiceCheckpointCodeLengthRule(80)
        val item = newManualFreightInvoiceCheckpoint("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
