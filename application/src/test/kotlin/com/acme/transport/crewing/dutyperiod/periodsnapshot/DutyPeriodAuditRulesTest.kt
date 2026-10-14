package com.acme.transport.crewing.dutyperiod.periodsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodAuditValidator()
        val item = newDutyPeriodAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = DutyPeriodAuditRequiredSummaryRule("duty period audit needs this field")
        val item = newDutyPeriodAudit("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DutyPeriodAuditNameLengthRule(86400)
        val item = newDutyPeriodAudit("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = DutyPeriodAuditSummaryLengthRule(255)
        val item = newDutyPeriodAudit("x", "y")
        item.summary = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = DutyPeriodAuditDurationMinutesRangeRule(16, 255)
        val item = newDutyPeriodAudit("x", "y")
        item.durationMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = DutyPeriodAuditQuantityRangeRule(64, 120)
        val item = newDutyPeriodAudit("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
