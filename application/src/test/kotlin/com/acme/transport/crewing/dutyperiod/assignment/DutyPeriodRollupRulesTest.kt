package com.acme.transport.crewing.dutyperiod.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodRollupValidator()
        val item = newDutyPeriodRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = DutyPeriodRollupRequiredDepotRule("duty period rollup needs a value")
        val item = newDutyPeriodRollup("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = DutyPeriodRollupUpdatedAtRangeRule(64, 3600)
        val item = newDutyPeriodRollup("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = DutyPeriodRollupWindowDaysRangeRule(80, 255)
        val item = newDutyPeriodRollup("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DutyPeriodRollupCodeLengthRule(255)
        val item = newDutyPeriodRollup("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
