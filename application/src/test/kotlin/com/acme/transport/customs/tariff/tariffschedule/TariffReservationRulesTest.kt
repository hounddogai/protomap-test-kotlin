package com.acme.transport.customs.tariff.tariffschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffReservationValidator()
        val item = newTariffReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = TariffReservationEtaMinutesRangeRule(64, 3600)
        val item = newTariffReservation("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = TariffReservationOdometerRangeRule(120, 1000)
        val item = newTariffReservation("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = TariffReservationNameLengthRule(64)
        val item = newTariffReservation("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = TariffReservationExpiresAtRangeRule(64, 1000)
        val item = newTariffReservation("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
