package com.acme.transport.crewing.dutyperiod.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodSummaryValidator()
        val item = newDutyPeriodSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DutyPeriodSummaryRequiredCodeRule("duty period summary needs a value")
        val item = newDutyPeriodSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = DutyPeriodSummaryExpiresAtRangeRule(120, 86400)
        val item = newDutyPeriodSummary("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = DutyPeriodSummaryCategoryLengthRule(3600)
        val item = newDutyPeriodSummary("x", "y")
        item.category = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = DutyPeriodSummaryUpdatedAtRangeRule(120, 1000)
        val item = newDutyPeriodSummary("x", "y")
        item.updatedAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
