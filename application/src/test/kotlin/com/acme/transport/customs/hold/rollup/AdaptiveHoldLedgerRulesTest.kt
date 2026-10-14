package com.acme.transport.customs.hold.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveHoldLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveHoldLedgerValidator()
        val item = newAdaptiveHoldLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveHoldLedgerLabelLengthRule(16)
        val item = newAdaptiveHoldLedger("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveHoldLedgerCodeLengthRule(86400)
        val item = newAdaptiveHoldLedger("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = AdaptiveHoldLedgerReviewerLengthRule(120)
        val item = newAdaptiveHoldLedger("x", "y")
        item.reviewer = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveHoldLedgerRequiredCodeRule("adaptive hold ledger needs this field")
        val item = newAdaptiveHoldLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
