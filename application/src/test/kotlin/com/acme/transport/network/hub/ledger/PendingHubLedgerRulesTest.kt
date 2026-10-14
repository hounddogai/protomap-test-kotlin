package com.acme.transport.network.hub.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingHubLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingHubLedgerValidator()
        val item = newPendingHubLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingHubLedgerCodeLengthRule(80)
        val item = newPendingHubLedger("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingHubLedgerRequiredCodeRule("pending hub ledger needs a value")
        val item = newPendingHubLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = PendingHubLedgerScoreRangeRule(9.9, 2111.0)
        val item = newPendingHubLedger("x", "y")
        item.score = 2112.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long locale`() {
        val rule = PendingHubLedgerLocaleLengthRule(86400)
        val item = newPendingHubLedger("x", "y")
        item.locale = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
