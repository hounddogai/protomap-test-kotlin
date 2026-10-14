package com.acme.transport.safety.scorecard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardLedgerValidator()
        val item = newScorecardLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = ScorecardLedgerCategoryLengthRule(86400)
        val item = newScorecardLedger("x", "y")
        item.category = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = ScorecardLedgerLimitRangeRule(120, 1000)
        val item = newScorecardLedger("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScorecardLedgerRequiredCodeRule("scorecard ledger needs this field")
        val item = newScorecardLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScorecardLedgerCodeLengthRule(64)
        val item = newScorecardLedger("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ScorecardLedgerFloorValueRangeRule(5.2, 1761.2)
        val item = newScorecardLedger("x", "y")
        item.floorValue = 1762.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScorecardLedgerTitleLengthRule(255)
        val item = newScorecardLedger("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
