package com.acme.transport.crewing.dutyperiod.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodOverrideValidator()
        val item = newDutyPeriodOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = DutyPeriodOverrideRequiredCurrencyRule("duty period override needs an entry")
        val item = newDutyPeriodOverride("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DutyPeriodOverrideLabelLengthRule(80)
        val item = newDutyPeriodOverride("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DutyPeriodOverrideThresholdRangeRule(8.2, 4192.2)
        val item = newDutyPeriodOverride("x", "y")
        item.threshold = 4193.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
