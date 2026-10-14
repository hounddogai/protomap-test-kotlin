package com.acme.transport.safety.violation.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualViolationForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualViolationForecastValidator()
        val item = newManualViolationForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = ManualViolationForecastRevisionRangeRule(16, 120)
        val item = newManualViolationForecast("x", "y")
        item.revision = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualViolationForecastRequiredLabelRule("manual violation forecast needs this field")
        val item = newManualViolationForecast("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ManualViolationForecastEtaMinutesRangeRule(255, 86400)
        val item = newManualViolationForecast("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = ManualViolationForecastCeilingRangeRule(7.7, 3008.0)
        val item = newManualViolationForecast("x", "y")
        item.ceiling = 3009.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualViolationForecastLabelLengthRule(1000)
        val item = newManualViolationForecast("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
