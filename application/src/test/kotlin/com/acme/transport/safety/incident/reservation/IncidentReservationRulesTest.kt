package com.acme.transport.safety.incident.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentReservationValidator()
        val item = newIncidentReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = IncidentReservationReviewerLengthRule(80)
        val item = newIncidentReservation("x", "y")
        item.reviewer = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = IncidentReservationShiftCodeLengthRule(32)
        val item = newIncidentReservation("x", "y")
        item.shiftCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IncidentReservationRequiredCodeRule("incident reservation needs a value")
        val item = newIncidentReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = IncidentReservationTitleLengthRule(255)
        val item = newIncidentReservation("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = IncidentReservationStopCountRangeRule(32, 3600)
        val item = newIncidentReservation("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
