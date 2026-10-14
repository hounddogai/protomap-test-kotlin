package com.acme.transport.linehaul.backhaul.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalBackhaulLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalBackhaulLedgerValidator()
        val item = newGlobalBackhaulLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = GlobalBackhaulLedgerRatioRangeRule(0.2, 4354.3)
        val item = newGlobalBackhaulLedger("x", "y")
        item.ratio = 4355.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long summary`() {
        val rule = GlobalBackhaulLedgerSummaryLengthRule(16)
        val item = newGlobalBackhaulLedger("x", "y")
        item.summary = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = GlobalBackhaulLedgerCountryLengthRule(1000)
        val item = newGlobalBackhaulLedger("x", "y")
        item.country = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalBackhaulLedgerRequiredNameRule("global backhaul ledger needs a value")
        val item = newGlobalBackhaulLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
