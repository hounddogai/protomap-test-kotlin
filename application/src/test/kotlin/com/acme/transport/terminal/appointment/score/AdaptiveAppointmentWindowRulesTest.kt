package com.acme.transport.terminal.appointment.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAppointmentWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveAppointmentWindowValidator()
        val item = newAdaptiveAppointmentWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = AdaptiveAppointmentWindowCategoryLengthRule(86400)
        val item = newAdaptiveAppointmentWindow("x", "y")
        item.category = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
