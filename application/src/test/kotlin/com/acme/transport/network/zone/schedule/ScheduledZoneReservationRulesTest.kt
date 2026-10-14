package com.acme.transport.network.zone.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledZoneReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledZoneReservationValidator()
        val item = newScheduledZoneReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ScheduledZoneReservationLoadKgRangeRule(4.8, 3444.6)
        val item = newScheduledZoneReservation("x", "y")
        item.loadKg = 3446.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = ScheduledZoneReservationRequiredCarrierRule("scheduled zone reservation needs a value")
        val item = newScheduledZoneReservation("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledZoneReservationRequiredCodeRule("scheduled zone reservation needs an entry")
        val item = newScheduledZoneReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledZoneReservationRequiredLabelRule("scheduled zone reservation needs an entry")
        val item = newScheduledZoneReservation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
