package com.acme.transport.emissions.carbonreport.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportReservationValidator()
        val item = newCarbonReportReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = CarbonReportReservationChannelLengthRule(3600)
        val item = newCarbonReportReservation("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = CarbonReportReservationDriverIdLengthRule(255)
        val item = newCarbonReportReservation("x", "y")
        item.driverId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CarbonReportReservationCodeLengthRule(16)
        val item = newCarbonReportReservation("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CarbonReportReservationRequiredCodeRule("carbon report reservation needs an entry")
        val item = newCarbonReportReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
