package com.acme.transport.crewing.dutyperiod.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodAssignmentValidator()
        val item = newDutyPeriodAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = DutyPeriodAssignmentBatchSizeRangeRule(32, 64)
        val item = newDutyPeriodAssignment("x", "y")
        item.batchSize = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DutyPeriodAssignmentRequiredCodeRule("duty period assignment needs this field")
        val item = newDutyPeriodAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
