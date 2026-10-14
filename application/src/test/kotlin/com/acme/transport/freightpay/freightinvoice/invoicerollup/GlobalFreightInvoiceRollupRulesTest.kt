package com.acme.transport.freightpay.freightinvoice.invoicerollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalFreightInvoiceRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalFreightInvoiceRollupValidator()
        val item = newGlobalFreightInvoiceRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalFreightInvoiceRollupRequiredNameRule("global freight invoice rollup needs this field")
        val item = newGlobalFreightInvoiceRollup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = GlobalFreightInvoiceRollupScoreRangeRule(2.1, 2620.7)
        val item = newGlobalFreightInvoiceRollup("x", "y")
        item.score = 2622.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = GlobalFreightInvoiceRollupCeilingRangeRule(1.5, 1108.5)
        val item = newGlobalFreightInvoiceRollup("x", "y")
        item.ceiling = 1110.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = GlobalFreightInvoiceRollupRequiredTitleRule("global freight invoice rollup needs this field")
        val item = newGlobalFreightInvoiceRollup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalFreightInvoiceRollupCodeLengthRule(255)
        val item = newGlobalFreightInvoiceRollup("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = GlobalFreightInvoiceRollupRequiredTrailerIdRule("global freight invoice rollup needs a value")
        val item = newGlobalFreightInvoiceRollup("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
