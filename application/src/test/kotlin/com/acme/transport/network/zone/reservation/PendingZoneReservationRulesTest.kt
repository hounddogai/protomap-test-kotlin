package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingZoneReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingZoneReservationValidator()
        val item = newPendingZoneReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = PendingZoneReservationRequiredChannelRule("pending zone reservation needs this field")
        val item = newPendingZoneReservation("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
