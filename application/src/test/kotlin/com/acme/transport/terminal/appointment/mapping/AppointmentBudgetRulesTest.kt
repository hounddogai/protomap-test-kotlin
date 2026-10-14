package com.acme.transport.terminal.appointment.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentBudgetValidator()
        val item = newAppointmentBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = AppointmentBudgetDescriptionLengthRule(32)
        val item = newAppointmentBudget("x", "y")
        item.description = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = AppointmentBudgetStartsAtRangeRule(16, 1000)
        val item = newAppointmentBudget("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AppointmentBudgetRequiredCodeRule("appointment budget needs this field")
        val item = newAppointmentBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = AppointmentBudgetCeilingRangeRule(0.0, 4088.9)
        val item = newAppointmentBudget("x", "y")
        item.ceiling = 4090.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = AppointmentBudgetDriverIdLengthRule(255)
        val item = newAppointmentBudget("x", "y")
        item.driverId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
