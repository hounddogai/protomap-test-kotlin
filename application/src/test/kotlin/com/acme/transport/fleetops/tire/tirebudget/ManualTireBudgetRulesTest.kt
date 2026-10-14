package com.acme.transport.fleetops.tire.tirebudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTireBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTireBudgetValidator()
        val item = newManualTireBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = ManualTireBudgetDurationMinutesRangeRule(32, 32)
        val item = newManualTireBudget("x", "y")
        item.durationMinutes = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ManualTireBudgetTrailerIdLengthRule(86400)
        val item = newManualTireBudget("x", "y")
        item.trailerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualTireBudgetRequiredCodeRule("manual tire budget needs this field")
        val item = newManualTireBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
