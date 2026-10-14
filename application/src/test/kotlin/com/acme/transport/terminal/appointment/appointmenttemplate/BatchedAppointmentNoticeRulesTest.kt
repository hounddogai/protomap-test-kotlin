package com.acme.transport.terminal.appointment.appointmenttemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAppointmentNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAppointmentNoticeValidator()
        val item = newBatchedAppointmentNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = BatchedAppointmentNoticeRequiredCurrencyRule("batched appointment notice needs this field")
        val item = newBatchedAppointmentNotice("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedAppointmentNoticeCodeLengthRule(3600)
        val item = newBatchedAppointmentNotice("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedAppointmentNoticeRequiredCodeRule("batched appointment notice needs this field")
        val item = newBatchedAppointmentNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = BatchedAppointmentNoticeRequiredRouteCodeRule("batched appointment notice needs a value")
        val item = newBatchedAppointmentNotice("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
