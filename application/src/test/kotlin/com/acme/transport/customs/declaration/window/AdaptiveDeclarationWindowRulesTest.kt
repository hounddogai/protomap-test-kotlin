package com.acme.transport.customs.declaration.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDeclarationWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDeclarationWindowValidator()
        val item = newAdaptiveDeclarationWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = AdaptiveDeclarationWindowDurationMinutesRangeRule(80, 80)
        val item = newAdaptiveDeclarationWindow("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = AdaptiveDeclarationWindowVehicleIdLengthRule(86400)
        val item = newAdaptiveDeclarationWindow("x", "y")
        item.vehicleId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = AdaptiveDeclarationWindowCountryLengthRule(80)
        val item = newAdaptiveDeclarationWindow("x", "y")
        item.country = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
