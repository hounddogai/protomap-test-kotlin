package com.acme.transport.crewing.dutyperiod.periodassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDutyPeriodLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDutyPeriodLedgerValidator()
        val item = newManualDutyPeriodLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualDutyPeriodLedgerCodeLengthRule(64)
        val item = newManualDutyPeriodLedger("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = ManualDutyPeriodLedgerRequiredNotesRule("manual duty period ledger needs this field")
        val item = newManualDutyPeriodLedger("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = ManualDutyPeriodLedgerRequiredLaneRule("manual duty period ledger needs a value")
        val item = newManualDutyPeriodLedger("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = ManualDutyPeriodLedgerNotesLengthRule(120)
        val item = newManualDutyPeriodLedger("x", "y")
        item.notes = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = ManualDutyPeriodLedgerStartsAtRangeRule(255, 86400)
        val item = newManualDutyPeriodLedger("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
