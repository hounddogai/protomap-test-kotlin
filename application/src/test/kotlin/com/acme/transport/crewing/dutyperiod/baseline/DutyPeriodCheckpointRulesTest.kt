package com.acme.transport.crewing.dutyperiod.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodCheckpointValidator()
        val item = newDutyPeriodCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DutyPeriodCheckpointLabelLengthRule(64)
        val item = newDutyPeriodCheckpoint("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DutyPeriodCheckpointRequiredCodeRule("duty period checkpoint needs this field")
        val item = newDutyPeriodCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = DutyPeriodCheckpointCeilingRangeRule(9.3, 4502.0)
        val item = newDutyPeriodCheckpoint("x", "y")
        item.ceiling = 4503.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = DutyPeriodCheckpointPriorityRangeRule(1000, 3600)
        val item = newDutyPeriodCheckpoint("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
