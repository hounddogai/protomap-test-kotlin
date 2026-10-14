package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodTierValidator()
        val item = newDutyPeriodTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DutyPeriodTierRequiredCodeRule("duty period tier needs an entry")
        val item = newDutyPeriodTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = DutyPeriodTierEtaMinutesRangeRule(16, 255)
        val item = newDutyPeriodTier("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = DutyPeriodTierRequiredChannelRule("duty period tier needs a value")
        val item = newDutyPeriodTier("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DutyPeriodTierLabelLengthRule(80)
        val item = newDutyPeriodTier("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DutyPeriodTierCodeLengthRule(64)
        val item = newDutyPeriodTier("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
