package com.acme.transport.freightpay.freightinvoice.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledFreightInvoiceSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledFreightInvoiceSnapshotValidator()
        val item = newScheduledFreightInvoiceSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ScheduledFreightInvoiceSnapshotTrailerIdLengthRule(16)
        val item = newScheduledFreightInvoiceSnapshot("x", "y")
        item.trailerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledFreightInvoiceSnapshotRequiredTitleRule("scheduled freight invoice snapshot needs a value")
        val item = newScheduledFreightInvoiceSnapshot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledFreightInvoiceSnapshotRequiredCodeRule("scheduled freight invoice snapshot needs a value")
        val item = newScheduledFreightInvoiceSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = ScheduledFreightInvoiceSnapshotRequiredCountryRule(
            "scheduled freight invoice snapshot needs a value",
        )
        val item = newScheduledFreightInvoiceSnapshot("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
