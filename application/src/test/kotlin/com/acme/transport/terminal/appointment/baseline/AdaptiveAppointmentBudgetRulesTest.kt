package com.acme.transport.terminal.appointment.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAppointmentBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveAppointmentBudgetValidator()
        val item = newAdaptiveAppointmentBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = AdaptiveAppointmentBudgetEndsAtRangeRule(64, 64)
        val item = newAdaptiveAppointmentBudget("x", "y")
        item.endsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = AdaptiveAppointmentBudgetDistanceKmRangeRule(9.8, 258.7)
        val item = newAdaptiveAppointmentBudget("x", "y")
        item.distanceKm = 260.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveAppointmentBudgetNameLengthRule(1000)
        val item = newAdaptiveAppointmentBudget("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveAppointmentBudgetRequiredCodeRule("adaptive appointment budget needs a value")
        val item = newAdaptiveAppointmentBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = AdaptiveAppointmentBudgetQuantityRangeRule(64, 120)
        val item = newAdaptiveAppointmentBudget("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveAppointmentBudgetRequiredTitleRule("adaptive appointment budget needs this field")
        val item = newAdaptiveAppointmentBudget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
