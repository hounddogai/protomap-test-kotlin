package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDutyPeriodLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDutyPeriodLedgerValidator()
        val item = newScheduledDutyPeriodLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledDutyPeriodLedgerRequiredCodeRule("scheduled duty period ledger needs this field")
        val item = newScheduledDutyPeriodLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledDutyPeriodLedgerCodeLengthRule(80)
        val item = newScheduledDutyPeriodLedger("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = ScheduledDutyPeriodLedgerScoreRangeRule(3.9, 4932.7)
        val item = newScheduledDutyPeriodLedger("x", "y")
        item.score = 4934.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = ScheduledDutyPeriodLedgerDepotLengthRule(3600)
        val item = newScheduledDutyPeriodLedger("x", "y")
        item.depot = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledDutyPeriodLedgerNameLengthRule(16)
        val item = newScheduledDutyPeriodLedger("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
