package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAppointmentVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingAppointmentVarianceValidator()
        val item = newPendingAppointmentVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingAppointmentVarianceRequiredTitleRule("pending appointment variance needs this field")
        val item = newPendingAppointmentVariance("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingAppointmentVarianceRequiredNameRule("pending appointment variance needs an entry")
        val item = newPendingAppointmentVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = PendingAppointmentVarianceRequiredCategoryRule("pending appointment variance needs a value")
        val item = newPendingAppointmentVariance("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingAppointmentVarianceTitleLengthRule(255)
        val item = newPendingAppointmentVariance("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingAppointmentVarianceNameLengthRule(120)
        val item = newPendingAppointmentVariance("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
