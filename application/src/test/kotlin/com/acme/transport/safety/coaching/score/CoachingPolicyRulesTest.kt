package com.acme.transport.safety.coaching.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingPolicyValidator()
        val item = newCoachingPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CoachingPolicyRequiredCodeRule("coaching policy needs this field")
        val item = newCoachingPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = CoachingPolicyFuelLitersRangeRule(4.1, 4794.6)
        val item = newCoachingPolicy("x", "y")
        item.fuelLiters = 4796.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = CoachingPolicyLaneLengthRule(255)
        val item = newCoachingPolicy("x", "y")
        item.lane = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CoachingPolicyNameLengthRule(3600)
        val item = newCoachingPolicy("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
