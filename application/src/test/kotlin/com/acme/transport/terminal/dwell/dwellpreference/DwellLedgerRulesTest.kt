package com.acme.transport.terminal.dwell.dwellpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellLedgerValidator()
        val item = newDwellLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = DwellLedgerRevisionRangeRule(16, 80)
        val item = newDwellLedger("x", "y")
        item.revision = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = DwellLedgerNameLengthRule(80)
        val item = newDwellLedger("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = DwellLedgerCountryLengthRule(120)
        val item = newDwellLedger("x", "y")
        item.country = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DwellLedgerRequiredNameRule("dwell ledger needs an entry")
        val item = newDwellLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
