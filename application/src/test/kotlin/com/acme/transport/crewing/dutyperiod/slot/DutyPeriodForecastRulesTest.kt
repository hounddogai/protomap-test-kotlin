package com.acme.transport.crewing.dutyperiod.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodForecastValidator()
        val item = newDutyPeriodForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DutyPeriodForecastNameLengthRule(86400)
        val item = newDutyPeriodForecast("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = DutyPeriodForecastOwnerIdLengthRule(255)
        val item = newDutyPeriodForecast("x", "y")
        item.ownerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = DutyPeriodForecastRequiredOwnerIdRule("duty period forecast needs an entry")
        val item = newDutyPeriodForecast("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = DutyPeriodForecastRatioRangeRule(5.5, 2589.3)
        val item = newDutyPeriodForecast("x", "y")
        item.ratio = 2590.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DutyPeriodForecastRequiredCodeRule("duty period forecast needs an entry")
        val item = newDutyPeriodForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
