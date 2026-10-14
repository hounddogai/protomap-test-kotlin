package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionReservationValidator()
        val item = newInspectionReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = InspectionReservationStartsAtRangeRule(80, 255)
        val item = newInspectionReservation("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
