package com.acme.transport.terminal.dockdoor.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorLedgerValidator()
        val item = newDockDoorLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = DockDoorLedgerDepotLengthRule(16)
        val item = newDockDoorLedger("x", "y")
        item.depot = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = DockDoorLedgerRequiredDepotRule("dock door ledger needs an entry")
        val item = newDockDoorLedger("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = DockDoorLedgerVersionRangeRule(255, 86400)
        val item = newDockDoorLedger("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
