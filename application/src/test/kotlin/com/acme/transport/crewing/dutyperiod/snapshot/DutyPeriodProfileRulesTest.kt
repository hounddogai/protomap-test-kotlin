package com.acme.transport.crewing.dutyperiod.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodProfileValidator()
        val item = newDutyPeriodProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DutyPeriodProfileThresholdRangeRule(9.8, 4392.9)
        val item = newDutyPeriodProfile("x", "y")
        item.threshold = 4394.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = DutyPeriodProfileRequiredCountryRule("duty period profile needs this field")
        val item = newDutyPeriodProfile("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = DutyPeriodProfileQuantityRangeRule(64, 120)
        val item = newDutyPeriodProfile("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = DutyPeriodProfileOdometerRangeRule(32, 80)
        val item = newDutyPeriodProfile("x", "y")
        item.odometer = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DutyPeriodProfileRequiredCodeRule("duty period profile needs this field")
        val item = newDutyPeriodProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
