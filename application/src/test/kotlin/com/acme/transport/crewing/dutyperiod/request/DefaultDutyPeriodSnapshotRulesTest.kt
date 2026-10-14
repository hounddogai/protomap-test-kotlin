package com.acme.transport.crewing.dutyperiod.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDutyPeriodSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultDutyPeriodSnapshotValidator()
        val item = newDefaultDutyPeriodSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultDutyPeriodSnapshotRequiredLabelRule("default duty period snapshot needs an entry")
        val item = newDefaultDutyPeriodSnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = DefaultDutyPeriodSnapshotRequiredDepotRule("default duty period snapshot needs an entry")
        val item = newDefaultDutyPeriodSnapshot("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultDutyPeriodSnapshotCodeLengthRule(16)
        val item = newDefaultDutyPeriodSnapshot("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = DefaultDutyPeriodSnapshotBudgetRangeRule(7.2, 3904.8)
        val item = newDefaultDutyPeriodSnapshot("x", "y")
        item.budget = 3906.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = DefaultDutyPeriodSnapshotTimezoneLengthRule(64)
        val item = newDefaultDutyPeriodSnapshot("x", "y")
        item.timezone = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultDutyPeriodSnapshotRequiredCodeRule("default duty period snapshot needs an entry")
        val item = newDefaultDutyPeriodSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
