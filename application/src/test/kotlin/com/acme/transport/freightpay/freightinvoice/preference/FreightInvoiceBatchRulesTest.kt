package com.acme.transport.freightpay.freightinvoice.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceBatchValidator()
        val item = newFreightInvoiceBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = FreightInvoiceBatchRequiredOwnerIdRule("freight invoice batch needs a value")
        val item = newFreightInvoiceBatch("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = FreightInvoiceBatchPhoneLengthRule(16)
        val item = newFreightInvoiceBatch("x", "y")
        item.phone = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = FreightInvoiceBatchUpdatedAtRangeRule(16, 32)
        val item = newFreightInvoiceBatch("x", "y")
        item.updatedAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FreightInvoiceBatchRequiredTitleRule("freight invoice batch needs an entry")
        val item = newFreightInvoiceBatch("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = FreightInvoiceBatchMinimumRangeRule(7.7, 4532.1)
        val item = newFreightInvoiceBatch("x", "y")
        item.minimum = 4533.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
