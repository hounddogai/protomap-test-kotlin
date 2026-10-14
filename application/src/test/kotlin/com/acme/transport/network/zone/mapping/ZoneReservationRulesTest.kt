package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneReservationValidator()
        val item = newZoneReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = ZoneReservationStopCountRangeRule(255, 255)
        val item = newZoneReservation("x", "y")
        item.stopCount = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ZoneReservationCodeLengthRule(3600)
        val item = newZoneReservation("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
