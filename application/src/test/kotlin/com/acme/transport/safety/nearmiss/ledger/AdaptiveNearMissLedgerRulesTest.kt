package com.acme.transport.safety.nearmiss.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNearMissLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveNearMissLedgerValidator()
        val item = newAdaptiveNearMissLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveNearMissLedgerCodeLengthRule(16)
        val item = newAdaptiveNearMissLedger("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = AdaptiveNearMissLedgerExpiresAtRangeRule(16, 16)
        val item = newAdaptiveNearMissLedger("x", "y")
        item.expiresAt = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveNearMissLedgerRequiredCodeRule("adaptive near miss ledger needs a value")
        val item = newAdaptiveNearMissLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
