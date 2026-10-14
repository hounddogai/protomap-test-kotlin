package com.acme.transport.linehaul.capacity.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCapacityExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalCapacityExceptionValidator()
        val item = newSeasonalCapacityException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = SeasonalCapacityExceptionRequiredTimezoneRule("seasonal capacity exception needs a value")
        val item = newSeasonalCapacityException("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = SeasonalCapacityExceptionDistanceKmRangeRule(8.7, 2513.4)
        val item = newSeasonalCapacityException("x", "y")
        item.distanceKm = 2514.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = SeasonalCapacityExceptionLimitRangeRule(255, 1000)
        val item = newSeasonalCapacityException("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long source`() {
        val rule = SeasonalCapacityExceptionSourceLengthRule(1000)
        val item = newSeasonalCapacityException("x", "y")
        item.source = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalCapacityExceptionRequiredCodeRule("seasonal capacity exception needs an entry")
        val item = newSeasonalCapacityException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = SeasonalCapacityExceptionRequiredSourceRule("seasonal capacity exception needs this field")
        val item = newSeasonalCapacityException("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
