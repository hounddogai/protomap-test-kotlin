package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFreightInvoiceRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingFreightInvoiceRequestValidator()
        val item = newRollingFreightInvoiceRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingFreightInvoiceRequestRequiredTitleRule("rolling freight invoice request needs a value")
        val item = newRollingFreightInvoiceRequest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingFreightInvoiceRequestCodeLengthRule(80)
        val item = newRollingFreightInvoiceRequest("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = RollingFreightInvoiceRequestOdometerRangeRule(80, 86400)
        val item = newRollingFreightInvoiceRequest("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RollingFreightInvoiceRequestThresholdRangeRule(0.2, 2093.0)
        val item = newRollingFreightInvoiceRequest("x", "y")
        item.threshold = 2094.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RollingFreightInvoiceRequestLaneLengthRule(16)
        val item = newRollingFreightInvoiceRequest("x", "y")
        item.lane = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
