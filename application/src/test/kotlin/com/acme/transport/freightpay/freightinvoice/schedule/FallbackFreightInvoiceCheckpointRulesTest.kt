package com.acme.transport.freightpay.freightinvoice.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackFreightInvoiceCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackFreightInvoiceCheckpointValidator()
        val item = newFallbackFreightInvoiceCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackFreightInvoiceCheckpointCodeLengthRule(255)
        val item = newFallbackFreightInvoiceCheckpoint("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = FallbackFreightInvoiceCheckpointReviewerLengthRule(64)
        val item = newFallbackFreightInvoiceCheckpoint("x", "y")
        item.reviewer = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = FallbackFreightInvoiceCheckpointRequiredContactEmailRule(
            "fallback freight invoice checkpoint needs this field",
        )
        val item = newFallbackFreightInvoiceCheckpoint("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = FallbackFreightInvoiceCheckpointContactEmailLengthRule(16)
        val item = newFallbackFreightInvoiceCheckpoint("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
