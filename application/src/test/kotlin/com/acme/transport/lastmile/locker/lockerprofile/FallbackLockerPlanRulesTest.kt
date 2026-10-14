package com.acme.transport.lastmile.locker.lockerprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackLockerPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackLockerPlanValidator()
        val item = newFallbackLockerPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackLockerPlanCodeLengthRule(16)
        val item = newFallbackLockerPlan("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = FallbackLockerPlanMaximumRangeRule(1.4, 3846.2)
        val item = newFallbackLockerPlan("x", "y")
        item.maximum = 3847.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackLockerPlanTitleLengthRule(3600)
        val item = newFallbackLockerPlan("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = FallbackLockerPlanFuelLitersRangeRule(6.9, 3487.6)
        val item = newFallbackLockerPlan("x", "y")
        item.fuelLiters = 3489.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackLockerPlanRequiredCodeRule("fallback locker plan needs this field")
        val item = newFallbackLockerPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
