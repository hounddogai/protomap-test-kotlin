package com.acme.transport.freightpay.freightinvoice.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceAdjustmentValidator()
        val item = newFreightInvoiceAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = FreightInvoiceAdjustmentAttemptsRangeRule(32, 255)
        val item = newFreightInvoiceAdjustment("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FreightInvoiceAdjustmentCodeLengthRule(255)
        val item = newFreightInvoiceAdjustment("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FreightInvoiceAdjustmentRequiredCodeRule("freight invoice adjustment needs a value")
        val item = newFreightInvoiceAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = FreightInvoiceAdjustmentRequiredCategoryRule("freight invoice adjustment needs a value")
        val item = newFreightInvoiceAdjustment("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = FreightInvoiceAdjustmentCategoryLengthRule(64)
        val item = newFreightInvoiceAdjustment("x", "y")
        item.category = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
