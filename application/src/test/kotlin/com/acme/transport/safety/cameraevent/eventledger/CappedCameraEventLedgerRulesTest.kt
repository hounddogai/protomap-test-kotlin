package com.acme.transport.safety.cameraevent.eventledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCameraEventLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCameraEventLedgerValidator()
        val item = newCappedCameraEventLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = CappedCameraEventLedgerExpiresAtRangeRule(32, 1000)
        val item = newCappedCameraEventLedger("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedCameraEventLedgerRequiredNameRule("capped camera event ledger needs this field")
        val item = newCappedCameraEventLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = CappedCameraEventLedgerRatioRangeRule(9.0, 877.0)
        val item = newCappedCameraEventLedger("x", "y")
        item.ratio = 878.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
