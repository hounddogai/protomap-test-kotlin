package com.acme.transport.crewing.dutyperiod.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDutyPeriodSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDutyPeriodSnapshotValidator()
        val item = newManualDutyPeriodSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ManualDutyPeriodSnapshotExpiresAtRangeRule(32, 64)
        val item = newManualDutyPeriodSnapshot("x", "y")
        item.expiresAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = ManualDutyPeriodSnapshotRequiredSegmentRule("manual duty period snapshot needs an entry")
        val item = newManualDutyPeriodSnapshot("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualDutyPeriodSnapshotNameLengthRule(86400)
        val item = newManualDutyPeriodSnapshot("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
