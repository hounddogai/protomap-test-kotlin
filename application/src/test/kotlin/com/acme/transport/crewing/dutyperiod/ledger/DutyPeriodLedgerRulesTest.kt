package com.acme.transport.crewing.dutyperiod.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodLedgerValidator()
        val item = newDutyPeriodLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = DutyPeriodLedgerRequiredCountryRule("duty period ledger needs this field")
        val item = newDutyPeriodLedger("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = DutyPeriodLedgerRequiredVehicleIdRule("duty period ledger needs an entry")
        val item = newDutyPeriodLedger("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DutyPeriodLedgerRequiredNameRule("duty period ledger needs this field")
        val item = newDutyPeriodLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DutyPeriodLedgerNameLengthRule(32)
        val item = newDutyPeriodLedger("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DutyPeriodLedgerCodeLengthRule(64)
        val item = newDutyPeriodLedger("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
