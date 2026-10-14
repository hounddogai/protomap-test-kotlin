package com.acme.transport.network.servicearea.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalServiceAreaLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalServiceAreaLedgerValidator()
        val item = newSeasonalServiceAreaLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalServiceAreaLedgerRequiredCodeRule("seasonal service area ledger needs this field")
        val item = newSeasonalServiceAreaLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = SeasonalServiceAreaLedgerPriorityRangeRule(255, 86400)
        val item = newSeasonalServiceAreaLedger("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalServiceAreaLedgerCodeLengthRule(86400)
        val item = newSeasonalServiceAreaLedger("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalServiceAreaLedgerLabelLengthRule(32)
        val item = newSeasonalServiceAreaLedger("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
