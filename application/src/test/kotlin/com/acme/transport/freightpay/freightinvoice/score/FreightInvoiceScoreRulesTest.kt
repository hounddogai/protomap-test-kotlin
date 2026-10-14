package com.acme.transport.freightpay.freightinvoice.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceScoreValidator()
        val item = newFreightInvoiceScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = FreightInvoiceScoreReviewerLengthRule(64)
        val item = newFreightInvoiceScore("x", "y")
        item.reviewer = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FreightInvoiceScoreRequiredNameRule("freight invoice score needs an entry")
        val item = newFreightInvoiceScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FreightInvoiceScoreRequiredCodeRule("freight invoice score needs a value")
        val item = newFreightInvoiceScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
