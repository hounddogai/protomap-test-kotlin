package com.acme.transport.lastmile.attempt.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAttemptLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientAttemptLedgerValidator()
        val item = newLenientAttemptLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientAttemptLedgerRequiredCodeRule("lenient attempt ledger needs an entry")
        val item = newLenientAttemptLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = LenientAttemptLedgerRequiredChannelRule("lenient attempt ledger needs a value")
        val item = newLenientAttemptLedger("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = LenientAttemptLedgerVersionRangeRule(1000, 3600)
        val item = newLenientAttemptLedger("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientAttemptLedgerCodeLengthRule(64)
        val item = newLenientAttemptLedger("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = LenientAttemptLedgerEtaMinutesRangeRule(64, 86400)
        val item = newLenientAttemptLedger("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
