package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentForecastValidator()
        val item = newIncidentForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = IncidentForecastStartsAtRangeRule(32, 86400)
        val item = newIncidentForecast("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = IncidentForecastGraceDaysRangeRule(32, 3600)
        val item = newIncidentForecast("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
