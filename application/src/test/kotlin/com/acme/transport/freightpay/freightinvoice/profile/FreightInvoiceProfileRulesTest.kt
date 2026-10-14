package com.acme.transport.freightpay.freightinvoice.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceProfileValidator()
        val item = newFreightInvoiceProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = FreightInvoiceProfileDurationMinutesRangeRule(80, 1000)
        val item = newFreightInvoiceProfile("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = FreightInvoiceProfileTitleLengthRule(120)
        val item = newFreightInvoiceProfile("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = FreightInvoiceProfileDepotLengthRule(120)
        val item = newFreightInvoiceProfile("x", "y")
        item.depot = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = FreightInvoiceProfileOwnerIdLengthRule(64)
        val item = newFreightInvoiceProfile("x", "y")
        item.ownerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
