package com.acme.transport.terminal.appointment.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAppointmentOverrideTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalAppointmentOverride("code-278", "label-174")
        assertEquals(SeasonalAppointmentOverrideStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newSeasonalAppointmentOverride("code-278", "label-174")
        try {
            item.moveTo(SeasonalAppointmentOverrideStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAppointmentOverrideStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalAppointmentOverride("code-278", "label-174")
        item.status = SeasonalAppointmentOverrideStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalAppointmentOverrideStatus.IDLE) }
    }
}
