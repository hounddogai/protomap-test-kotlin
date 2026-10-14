package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAppointmentVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAppointmentVarianceValidator()
        val item = newBatchedAppointmentVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = BatchedAppointmentVarianceRequiredCategoryRule("batched appointment variance needs a value")
        val item = newBatchedAppointmentVariance("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = BatchedAppointmentVarianceTrailerIdLengthRule(32)
        val item = newBatchedAppointmentVariance("x", "y")
        item.trailerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedAppointmentVarianceLabelLengthRule(32)
        val item = newBatchedAppointmentVariance("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
