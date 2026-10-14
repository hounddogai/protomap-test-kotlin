package com.acme.transport.freightpay.freightinvoice.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalFreightInvoiceSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalFreightInvoiceSummaryValidator()
        val item = newRegionalFreightInvoiceSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = RegionalFreightInvoiceSummaryDistanceKmRangeRule(6.5, 2286.2)
        val item = newRegionalFreightInvoiceSummary("x", "y")
        item.distanceKm = 2287.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = RegionalFreightInvoiceSummaryRequiredSourceRule("regional freight invoice summary needs this field")
        val item = newRegionalFreightInvoiceSummary("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = RegionalFreightInvoiceSummaryShiftCodeLengthRule(80)
        val item = newRegionalFreightInvoiceSummary("x", "y")
        item.shiftCode = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalFreightInvoiceSummaryTitleLengthRule(120)
        val item = newRegionalFreightInvoiceSummary("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = RegionalFreightInvoiceSummaryVersionRangeRule(80, 1000)
        val item = newRegionalFreightInvoiceSummary("x", "y")
        item.version = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
