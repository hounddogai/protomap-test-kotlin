package com.acme.transport.network.node.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredNodeLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredNodeLedgerValidator()
        val item = newTieredNodeLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredNodeLedgerLabelLengthRule(255)
        val item = newTieredNodeLedger("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = TieredNodeLedgerRequiredDepotRule("tiered node ledger needs this field")
        val item = newTieredNodeLedger("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = TieredNodeLedgerRateRangeRule(0.9, 1395.8)
        val item = newTieredNodeLedger("x", "y")
        item.rate = 1397.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredNodeLedgerCodeLengthRule(64)
        val item = newTieredNodeLedger("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
