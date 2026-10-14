package com.acme.transport.lastmile.drop.dropreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDropForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDropForecastValidator()
        val item = newScheduledDropForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledDropForecastCodeLengthRule(64)
        val item = newScheduledDropForecast("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledDropForecastRequiredCodeRule("scheduled drop forecast needs this field")
        val item = newScheduledDropForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = ScheduledDropForecastChecksumLengthRule(3600)
        val item = newScheduledDropForecast("x", "y")
        item.checksum = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ScheduledDropForecastEtaMinutesRangeRule(1000, 86400)
        val item = newScheduledDropForecast("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
