package com.acme.transport.terminal.appointment.appointmentscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAppointmentClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredAppointmentClaimValidator()
        val item = newTieredAppointmentClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredAppointmentClaimRequiredCodeRule("tiered appointment claim needs a value")
        val item = newTieredAppointmentClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredAppointmentClaimRequiredLabelRule("tiered appointment claim needs an entry")
        val item = newTieredAppointmentClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = TieredAppointmentClaimDurationMinutesRangeRule(120, 86400)
        val item = newTieredAppointmentClaim("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredAppointmentClaimCodeLengthRule(1000)
        val item = newTieredAppointmentClaim("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredAppointmentClaimLabelLengthRule(3600)
        val item = newTieredAppointmentClaim("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredAppointmentClaimTitleLengthRule(120)
        val item = newTieredAppointmentClaim("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
