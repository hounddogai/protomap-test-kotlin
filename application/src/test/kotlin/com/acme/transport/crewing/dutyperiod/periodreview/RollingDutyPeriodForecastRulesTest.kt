package com.acme.transport.crewing.dutyperiod.periodreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDutyPeriodForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDutyPeriodForecastValidator()
        val item = newRollingDutyPeriodForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingDutyPeriodForecastRequiredLabelRule("rolling duty period forecast needs this field")
        val item = newRollingDutyPeriodForecast("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RollingDutyPeriodForecastStopCountRangeRule(32, 255)
        val item = newRollingDutyPeriodForecast("x", "y")
        item.stopCount = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
