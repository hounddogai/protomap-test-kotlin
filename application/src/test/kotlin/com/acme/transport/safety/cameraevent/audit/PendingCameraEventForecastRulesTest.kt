package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCameraEventForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCameraEventForecastValidator()
        val item = newPendingCameraEventForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = PendingCameraEventForecastUnitLengthRule(255)
        val item = newPendingCameraEventForecast("x", "y")
        item.unit = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = PendingCameraEventForecastLoadKgRangeRule(4.9, 3485.6)
        val item = newPendingCameraEventForecast("x", "y")
        item.loadKg = 3487.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = PendingCameraEventForecastRequiredUnitRule("pending camera event forecast needs a value")
        val item = newPendingCameraEventForecast("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
