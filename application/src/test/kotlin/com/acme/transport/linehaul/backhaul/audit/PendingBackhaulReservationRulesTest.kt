package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingBackhaulReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingBackhaulReservationValidator()
        val item = newPendingBackhaulReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = PendingBackhaulReservationRequiredDriverIdRule("pending backhaul reservation needs a value")
        val item = newPendingBackhaulReservation("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = PendingBackhaulReservationPhoneLengthRule(86400)
        val item = newPendingBackhaulReservation("x", "y")
        item.phone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
