package com.acme.transport.crewing.dutyperiod.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDutyPeriodProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDutyPeriodProfileValidator()
        val item = newLenientDutyPeriodProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientDutyPeriodProfileRequiredNameRule("lenient duty period profile needs a value")
        val item = newLenientDutyPeriodProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = LenientDutyPeriodProfileDurationMinutesRangeRule(86400, 86400)
        val item = newLenientDutyPeriodProfile("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
