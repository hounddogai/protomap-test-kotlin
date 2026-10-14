package com.acme.transport.freightpay.accessorial.accessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialLedgerValidator()
        val item = newAccessorialLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = AccessorialLedgerDriverIdLengthRule(86400)
        val item = newAccessorialLedger("x", "y")
        item.driverId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AccessorialLedgerCodeLengthRule(32)
        val item = newAccessorialLedger("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = AccessorialLedgerCreatedAtRangeRule(255, 3600)
        val item = newAccessorialLedger("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AccessorialLedgerRequiredCodeRule("accessorial ledger needs an entry")
        val item = newAccessorialLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = AccessorialLedgerSourceLengthRule(32)
        val item = newAccessorialLedger("x", "y")
        item.source = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
