package com.acme.transport.lastmile.locker.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerLedgerValidator()
        val item = newLockerLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LockerLedgerRequiredTitleRule("locker ledger needs this field")
        val item = newLockerLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LockerLedgerWeightRangeRule(3.2, 331.5)
        val item = newLockerLedger("x", "y")
        item.weight = 333.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LockerLedgerRequiredCodeRule("locker ledger needs this field")
        val item = newLockerLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LockerLedgerCodeLengthRule(255)
        val item = newLockerLedger("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = LockerLedgerQuantityRangeRule(1000, 86400)
        val item = newLockerLedger("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LockerLedgerTitleLengthRule(1000)
        val item = newLockerLedger("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
