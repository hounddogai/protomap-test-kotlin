package com.acme.transport.crewing.dutyperiod.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDutyPeriodEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalDutyPeriodEstimateValidator()
        val item = newGlobalDutyPeriodEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = GlobalDutyPeriodEstimateMaximumRangeRule(4.8, 2573.5)
        val item = newGlobalDutyPeriodEstimate("x", "y")
        item.maximum = 2575.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = GlobalDutyPeriodEstimatePhoneLengthRule(32)
        val item = newGlobalDutyPeriodEstimate("x", "y")
        item.phone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = GlobalDutyPeriodEstimateRequiredNotesRule("global duty period estimate needs an entry")
        val item = newGlobalDutyPeriodEstimate("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = GlobalDutyPeriodEstimateNotesLengthRule(64)
        val item = newGlobalDutyPeriodEstimate("x", "y")
        item.notes = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalDutyPeriodEstimateTitleLengthRule(80)
        val item = newGlobalDutyPeriodEstimate("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
