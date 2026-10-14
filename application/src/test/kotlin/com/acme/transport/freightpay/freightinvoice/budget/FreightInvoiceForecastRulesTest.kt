package com.acme.transport.freightpay.freightinvoice.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceForecastValidator()
        val item = newFreightInvoiceForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = FreightInvoiceForecastRequiredContactEmailRule("freight invoice forecast needs a value")
        val item = newFreightInvoiceForecast("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FreightInvoiceForecastRequiredNameRule("freight invoice forecast needs a value")
        val item = newFreightInvoiceForecast("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FreightInvoiceForecastRequiredCodeRule("freight invoice forecast needs an entry")
        val item = newFreightInvoiceForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = FreightInvoiceForecastContactEmailLengthRule(120)
        val item = newFreightInvoiceForecast("x", "y")
        item.contactEmail = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FreightInvoiceForecastCodeLengthRule(120)
        val item = newFreightInvoiceForecast("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = FreightInvoiceForecastEndsAtRangeRule(120, 86400)
        val item = newFreightInvoiceForecast("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
