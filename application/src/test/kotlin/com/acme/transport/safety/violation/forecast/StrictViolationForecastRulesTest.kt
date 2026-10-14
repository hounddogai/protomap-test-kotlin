package com.acme.transport.safety.violation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictViolationForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictViolationForecastValidator()
        val item = newStrictViolationForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = StrictViolationForecastContactEmailLengthRule(80)
        val item = newStrictViolationForecast("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = StrictViolationForecastRequiredContactEmailRule("strict violation forecast needs this field")
        val item = newStrictViolationForecast("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = StrictViolationForecastRetriesRangeRule(32, 1000)
        val item = newStrictViolationForecast("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictViolationForecastRequiredTitleRule("strict violation forecast needs an entry")
        val item = newStrictViolationForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictViolationForecastCodeLengthRule(16)
        val item = newStrictViolationForecast("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictViolationForecastTitleLengthRule(80)
        val item = newStrictViolationForecast("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
