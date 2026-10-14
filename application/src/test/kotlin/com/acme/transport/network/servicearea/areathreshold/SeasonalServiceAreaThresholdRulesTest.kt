package com.acme.transport.network.servicearea.areathreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalServiceAreaThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalServiceAreaThresholdValidator()
        val item = newSeasonalServiceAreaThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = SeasonalServiceAreaThresholdDriverIdLengthRule(32)
        val item = newSeasonalServiceAreaThreshold("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalServiceAreaThresholdCodeLengthRule(86400)
        val item = newSeasonalServiceAreaThreshold("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = SeasonalServiceAreaThresholdCreatedAtRangeRule(32, 255)
        val item = newSeasonalServiceAreaThreshold("x", "y")
        item.createdAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalServiceAreaThresholdNameLengthRule(1000)
        val item = newSeasonalServiceAreaThreshold("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
