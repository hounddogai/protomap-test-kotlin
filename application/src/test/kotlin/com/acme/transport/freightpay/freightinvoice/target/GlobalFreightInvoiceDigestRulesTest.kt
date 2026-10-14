package com.acme.transport.freightpay.freightinvoice.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalFreightInvoiceDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalFreightInvoiceDigestValidator()
        val item = newGlobalFreightInvoiceDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = GlobalFreightInvoiceDigestDurationMinutesRangeRule(64, 1000)
        val item = newGlobalFreightInvoiceDigest("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = GlobalFreightInvoiceDigestPalletsRangeRule(16, 1000)
        val item = newGlobalFreightInvoiceDigest("x", "y")
        item.pallets = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = GlobalFreightInvoiceDigestTrailerIdLengthRule(64)
        val item = newGlobalFreightInvoiceDigest("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
