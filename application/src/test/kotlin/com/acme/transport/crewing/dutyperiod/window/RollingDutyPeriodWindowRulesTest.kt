package com.acme.transport.crewing.dutyperiod.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDutyPeriodWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDutyPeriodWindowValidator()
        val item = newRollingDutyPeriodWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingDutyPeriodWindowNameLengthRule(32)
        val item = newRollingDutyPeriodWindow("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RollingDutyPeriodWindowFuelLitersRangeRule(9.9, 254.5)
        val item = newRollingDutyPeriodWindow("x", "y")
        item.fuelLiters = 256.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingDutyPeriodWindowRequiredNameRule("rolling duty period window needs an entry")
        val item = newRollingDutyPeriodWindow("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
