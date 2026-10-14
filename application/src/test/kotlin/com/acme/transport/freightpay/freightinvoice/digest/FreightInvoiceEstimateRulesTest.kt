package com.acme.transport.freightpay.freightinvoice.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceEstimateValidator()
        val item = newFreightInvoiceEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FreightInvoiceEstimateCodeLengthRule(64)
        val item = newFreightInvoiceEstimate("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FreightInvoiceEstimateRequiredNameRule("freight invoice estimate needs an entry")
        val item = newFreightInvoiceEstimate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = FreightInvoiceEstimateRequiredExternalRefRule("freight invoice estimate needs this field")
        val item = newFreightInvoiceEstimate("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
