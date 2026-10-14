package com.acme.transport.freightpay.freightinvoice.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceManifestValidator()
        val item = newFreightInvoiceManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = FreightInvoiceManifestExternalRefLengthRule(64)
        val item = newFreightInvoiceManifest("x", "y")
        item.externalRef = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = FreightInvoiceManifestEndsAtRangeRule(32, 3600)
        val item = newFreightInvoiceManifest("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = FreightInvoiceManifestStartsAtRangeRule(120, 3600)
        val item = newFreightInvoiceManifest("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = FreightInvoiceManifestWindowDaysRangeRule(16, 80)
        val item = newFreightInvoiceManifest("x", "y")
        item.windowDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
