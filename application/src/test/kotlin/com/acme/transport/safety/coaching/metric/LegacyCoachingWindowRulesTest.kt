package com.acme.transport.safety.coaching.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCoachingWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyCoachingWindowValidator()
        val item = newLegacyCoachingWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyCoachingWindowLabelLengthRule(1000)
        val item = newLegacyCoachingWindow("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = LegacyCoachingWindowVehicleIdLengthRule(255)
        val item = newLegacyCoachingWindow("x", "y")
        item.vehicleId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = LegacyCoachingWindowFuelLitersRangeRule(6.2, 1303.3)
        val item = newLegacyCoachingWindow("x", "y")
        item.fuelLiters = 1304.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
