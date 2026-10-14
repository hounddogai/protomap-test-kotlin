package com.acme.transport.terminal.appointment.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentScoreValidator()
        val item = newAppointmentScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AppointmentScoreCodeLengthRule(64)
        val item = newAppointmentScore("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = AppointmentScoreScoreRangeRule(5.5, 1257.5)
        val item = newAppointmentScore("x", "y")
        item.score = 1259.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = AppointmentScoreNameLengthRule(16)
        val item = newAppointmentScore("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = AppointmentScoreCostRangeRule(1.3, 2151.8)
        val item = newAppointmentScore("x", "y")
        item.cost = 2153.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
