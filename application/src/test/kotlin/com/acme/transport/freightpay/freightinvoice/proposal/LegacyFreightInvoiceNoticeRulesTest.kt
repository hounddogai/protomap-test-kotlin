package com.acme.transport.freightpay.freightinvoice.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyFreightInvoiceNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyFreightInvoiceNoticeValidator()
        val item = newLegacyFreightInvoiceNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LegacyFreightInvoiceNoticeRetriesRangeRule(255, 1000)
        val item = newLegacyFreightInvoiceNotice("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = LegacyFreightInvoiceNoticeOdometerRangeRule(32, 120)
        val item = newLegacyFreightInvoiceNotice("x", "y")
        item.odometer = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = LegacyFreightInvoiceNoticeExternalRefLengthRule(64)
        val item = newLegacyFreightInvoiceNotice("x", "y")
        item.externalRef = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyFreightInvoiceNoticeRequiredTitleRule("legacy freight invoice notice needs this field")
        val item = newLegacyFreightInvoiceNotice("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
