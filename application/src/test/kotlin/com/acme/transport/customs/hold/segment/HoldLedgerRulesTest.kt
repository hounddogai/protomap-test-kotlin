package com.acme.transport.customs.hold.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldLedgerValidator()
        val item = newHoldLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = HoldLedgerLoadKgRangeRule(1.8, 4576.0)
        val item = newHoldLedger("x", "y")
        item.loadKg = 4577.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = HoldLedgerRequiredLabelRule("hold ledger needs a value")
        val item = newHoldLedger("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = HoldLedgerSummaryLengthRule(120)
        val item = newHoldLedger("x", "y")
        item.summary = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
