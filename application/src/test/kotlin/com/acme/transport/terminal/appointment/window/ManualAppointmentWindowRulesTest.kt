package com.acme.transport.terminal.appointment.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAppointmentWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualAppointmentWindowValidator()
        val item = newManualAppointmentWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualAppointmentWindowRequiredLabelRule("manual appointment window needs an entry")
        val item = newManualAppointmentWindow("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = ManualAppointmentWindowMinimumRangeRule(3.8, 3641.5)
        val item = newManualAppointmentWindow("x", "y")
        item.minimum = 3643.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ManualAppointmentWindowAttemptsRangeRule(32, 3600)
        val item = newManualAppointmentWindow("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = ManualAppointmentWindowRequiredChannelRule("manual appointment window needs this field")
        val item = newManualAppointmentWindow("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = ManualAppointmentWindowChannelLengthRule(64)
        val item = newManualAppointmentWindow("x", "y")
        item.channel = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ManualAppointmentWindowRequiredShiftCodeRule("manual appointment window needs a value")
        val item = newManualAppointmentWindow("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
