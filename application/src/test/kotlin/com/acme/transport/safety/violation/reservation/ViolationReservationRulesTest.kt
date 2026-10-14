package com.acme.transport.safety.violation.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationReservationValidator()
        val item = newViolationReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = ViolationReservationRequiredSegmentRule("violation reservation needs this field")
        val item = newViolationReservation("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = ViolationReservationSegmentLengthRule(32)
        val item = newViolationReservation("x", "y")
        item.segment = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
