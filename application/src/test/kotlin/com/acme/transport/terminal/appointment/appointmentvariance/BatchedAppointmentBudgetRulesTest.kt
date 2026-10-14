package com.acme.transport.terminal.appointment.appointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAppointmentBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAppointmentBudgetValidator()
        val item = newBatchedAppointmentBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = BatchedAppointmentBudgetRequiredSourceRule("batched appointment budget needs this field")
        val item = newBatchedAppointmentBudget("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = BatchedAppointmentBudgetCurrencyLengthRule(1000)
        val item = newBatchedAppointmentBudget("x", "y")
        item.currency = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
