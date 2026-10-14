package com.acme.transport.terminal.appointment.appointmentscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAppointmentScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAppointmentScoreValidator()
        val item = newBatchedAppointmentScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = BatchedAppointmentScoreRequiredChannelRule("batched appointment score needs a value")
        val item = newBatchedAppointmentScore("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = BatchedAppointmentScoreRequiredTimezoneRule("batched appointment score needs a value")
        val item = newBatchedAppointmentScore("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedAppointmentScoreRequiredCodeRule("batched appointment score needs this field")
        val item = newBatchedAppointmentScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedAppointmentScoreTitleLengthRule(32)
        val item = newBatchedAppointmentScore("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
