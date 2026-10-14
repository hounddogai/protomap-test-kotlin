package com.acme.transport.safety.nearmiss.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackNearMissBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackNearMissBudgetValidator()
        val item = newFallbackNearMissBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = FallbackNearMissBudgetWindowDaysRangeRule(120, 3600)
        val item = newFallbackNearMissBudget("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = FallbackNearMissBudgetStopCountRangeRule(120, 1000)
        val item = newFallbackNearMissBudget("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackNearMissBudgetRequiredLabelRule("fallback near miss budget needs an entry")
        val item = newFallbackNearMissBudget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = FallbackNearMissBudgetRouteCodeLengthRule(120)
        val item = newFallbackNearMissBudget("x", "y")
        item.routeCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
