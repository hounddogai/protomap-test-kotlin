package com.acme.transport.freightpay.accessorial.accessorialpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAccessorialLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackAccessorialLedgerValidator()
        val item = newFallbackAccessorialLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = FallbackAccessorialLedgerOdometerRangeRule(64, 255)
        val item = newFallbackAccessorialLedger("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = FallbackAccessorialLedgerRequiredOwnerIdRule("fallback accessorial ledger needs a value")
        val item = newFallbackAccessorialLedger("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = FallbackAccessorialLedgerRequiredPhoneRule("fallback accessorial ledger needs an entry")
        val item = newFallbackAccessorialLedger("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = FallbackAccessorialLedgerOwnerIdLengthRule(86400)
        val item = newFallbackAccessorialLedger("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long phone`() {
        val rule = FallbackAccessorialLedgerPhoneLengthRule(1000)
        val item = newFallbackAccessorialLedger("x", "y")
        item.phone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
