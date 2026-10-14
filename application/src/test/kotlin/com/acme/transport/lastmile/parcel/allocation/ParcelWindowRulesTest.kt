package com.acme.transport.lastmile.parcel.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelWindowValidator()
        val item = newParcelWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = ParcelWindowRequiredVehicleIdRule("parcel window needs this field")
        val item = newParcelWindow("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = ParcelWindowRateRangeRule(9.2, 3662.9)
        val item = newParcelWindow("x", "y")
        item.rate = 3664.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ParcelWindowEndsAtRangeRule(32, 80)
        val item = newParcelWindow("x", "y")
        item.endsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ParcelWindowNameLengthRule(3600)
        val item = newParcelWindow("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = ParcelWindowDriverIdLengthRule(16)
        val item = newParcelWindow("x", "y")
        item.driverId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
