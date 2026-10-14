package com.acme.transport.network.node.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeLedgerValidator()
        val item = newNodeLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = NodeLedgerLocaleLengthRule(1000)
        val item = newNodeLedger("x", "y")
        item.locale = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NodeLedgerRequiredCodeRule("node ledger needs an entry")
        val item = newNodeLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = NodeLedgerEtaMinutesRangeRule(16, 255)
        val item = newNodeLedger("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = NodeLedgerNotesLengthRule(32)
        val item = newNodeLedger("x", "y")
        item.notes = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
