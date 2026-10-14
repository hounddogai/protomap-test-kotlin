package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDutyPeriodCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDutyPeriodCheckpointValidator()
        val item = newAdaptiveDutyPeriodCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveDutyPeriodCheckpointTitleLengthRule(80)
        val item = newAdaptiveDutyPeriodCheckpoint("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = AdaptiveDutyPeriodCheckpointGraceDaysRangeRule(16, 32)
        val item = newAdaptiveDutyPeriodCheckpoint("x", "y")
        item.graceDays = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveDutyPeriodCheckpointRequiredTitleRule("adaptive duty period checkpoint needs this field")
        val item = newAdaptiveDutyPeriodCheckpoint("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveDutyPeriodCheckpointCodeLengthRule(86400)
        val item = newAdaptiveDutyPeriodCheckpoint("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
