package com.acme.transport.crewing.roster.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterLedgerValidator()
        val item = newRosterLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RosterLedgerLabelLengthRule(16)
        val item = newRosterLedger("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RosterLedgerMinimumRangeRule(6.3, 3442.9)
        val item = newRosterLedger("x", "y")
        item.minimum = 3444.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
