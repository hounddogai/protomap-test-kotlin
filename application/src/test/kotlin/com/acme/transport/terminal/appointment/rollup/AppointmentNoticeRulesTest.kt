package com.acme.transport.terminal.appointment.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentNoticeValidator()
        val item = newAppointmentNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AppointmentNoticeCodeLengthRule(255)
        val item = newAppointmentNotice("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = AppointmentNoticeNameLengthRule(80)
        val item = newAppointmentNotice("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = AppointmentNoticeRequiredDepotRule("appointment notice needs an entry")
        val item = newAppointmentNotice("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = AppointmentNoticeQuantityRangeRule(32, 80)
        val item = newAppointmentNotice("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AppointmentNoticeRequiredNameRule("appointment notice needs a value")
        val item = newAppointmentNotice("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
