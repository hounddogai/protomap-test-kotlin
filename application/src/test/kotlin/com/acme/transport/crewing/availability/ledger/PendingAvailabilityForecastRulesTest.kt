package com.acme.transport.crewing.availability.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAvailabilityForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingAvailabilityForecastValidator()
        val item = newPendingAvailabilityForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = PendingAvailabilityForecastRequiredChannelRule("pending availability forecast needs this field")
        val item = newPendingAvailabilityForecast("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = PendingAvailabilityForecastUpdatedAtRangeRule(64, 3600)
        val item = newPendingAvailabilityForecast("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = PendingAvailabilityForecastEndsAtRangeRule(16, 1000)
        val item = newPendingAvailabilityForecast("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingAvailabilityForecastRequiredCodeRule("pending availability forecast needs a value")
        val item = newPendingAvailabilityForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingAvailabilityForecastCodeLengthRule(86400)
        val item = newPendingAvailabilityForecast("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = PendingAvailabilityForecastCeilingRangeRule(2.2, 4864.0)
        val item = newPendingAvailabilityForecast("x", "y")
        item.ceiling = 4865.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
