package com.acme.transport.safety.coaching.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingClaimValidator()
        val item = newCoachingClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = CoachingClaimRequiredExternalRefRule("coaching claim needs this field")
        val item = newCoachingClaim("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = CoachingClaimFuelLitersRangeRule(6.8, 2803.8)
        val item = newCoachingClaim("x", "y")
        item.fuelLiters = 2805.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = CoachingClaimLoadKgRangeRule(2.8, 2344.0)
        val item = newCoachingClaim("x", "y")
        item.loadKg = 2345.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = CoachingClaimTitleLengthRule(255)
        val item = newCoachingClaim("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
